package dev.simplified.util.compression;

import dev.simplified.util.compression.exception.CompressionException;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

/**
 * Low-allocation zlib compress helpers that pre-size the deflate accumulator from zlib's
 * {@code deflateBound} upper bound.
 *
 * <p>Mirrors {@link GzipCompression}'s compress-side shape; the only differences are the
 * framing overhead (6 bytes for zlib vs 18 for gzip - zlib uses a 2-byte header and a 4-byte
 * Adler32 trailer per <a href="https://www.rfc-editor.org/rfc/rfc1950">RFC 1950</a>) and the
 * use of {@link DeflaterOutputStream} instead of {@link java.util.zip.GZIPOutputStream}.
 *
 * <p>This class intentionally has no {@code decompress} method. Zlib's trailer is a 4-byte
 * Adler32 checksum with no uncompressed-size field, so there is nothing analogous to gzip's
 * ISIZE trick available to size the inflate output buffer up front. Zlib decompression
 * continues to flow through {@link Compression#decompress(byte[])}'s growable accumulator
 * path, which is the only correct option.
 *
 * <p>Pre-sizes the {@link ByteArrayOutputStream} to the bound, eliminating the
 * {@link java.util.Arrays#copyOf} doubling sequence that the default 32-byte initial capacity
 * triggers on every non-trivial payload. The final {@link ByteArrayOutputStream#toByteArray}
 * call still trims, paying one trim copy - smaller win than gzip's inflate side, but real.
 *
 * <p>Most callers should go through {@link Compression#compress(byte[], Compression)}, which
 * dispatches here for the {@link Compression#ZLIB} case. Direct calls are fine when the format
 * is known to be zlib in advance.
 */
@UtilityClass
public final class ZlibCompression {

	/**
	 * Compresses {@code data} into a zlib-framed payload using the worst-case-bound accumulator.
	 *
	 * @param data the uncompressed payload
	 * @return the zlib-compressed bytes
	 * @throws CompressionException if an {@link IOException} escapes the underlying deflater
	 */
	public static byte @NotNull [] compress(byte @NotNull [] data) throws CompressionException {
		return compress(data, 0, data.length);
	}

	/**
	 * Compresses a slice of {@code data} into a zlib-framed payload.
	 *
	 * <p>Pre-sizes the {@link ByteArrayOutputStream} accumulator to zlib's {@code deflateBound}
	 * plus 6 bytes of zlib framing, so no growth copies occur during deflate. The final
	 * {@link ByteArrayOutputStream#toByteArray} call still trims the buffer to the actual
	 * compressed size, paying one trim copy.
	 *
	 * <p>The bound formula
	 * ({@code length + (length >> 12) + (length >> 14) + (length >> 25) + 13 + 6}) mirrors
	 * zlib's {@code deflateBound} for raw deflate plus 6 bytes for the zlib header (2) and
	 * Adler32 trailer (4). For zero-length input it returns 19 bytes (vs the actual 8-byte
	 * empty zlib payload) - a tight but provably safe upper bound.
	 *
	 * @param data the buffer containing the uncompressed payload
	 * @param offset the first valid byte in {@code data}
	 * @param length the number of valid bytes starting at {@code offset}
	 * @return the zlib-compressed bytes
	 * @throws CompressionException if an {@link IOException} escapes the underlying deflater
	 * @throws IndexOutOfBoundsException if {@code offset} or {@code length} address bytes
	 *     outside {@code data}, or either is negative
	 */
	public static byte @NotNull [] compress(byte @NotNull [] data, int offset, int length) throws CompressionException {
		java.util.Objects.checkFromIndexSize(offset, length, data.length);

		int bound = worstCaseDeflateBound(length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bound);

		try (DeflaterOutputStream def = new DeflaterOutputStream(baos)) {
			def.write(data, offset, length);
		} catch (IOException exception) {
			throw new CompressionException(exception, "Zlib deflate failed");
		}

		return baos.toByteArray();
	}

	/**
	 * Computes the worst-case zlib deflate output size for an input of {@code length} bytes.
	 *
	 * <p>Mirrors zlib's {@code deflateBound} formula exactly, then adds 6 bytes of zlib framing
	 * (2-byte CMF/FLG header plus 4-byte Adler32 trailer). The bound is a strict upper limit
	 * for the stock {@link java.util.zip.Deflater} compression at any level; the actual deflate
	 * output is smaller for any input that compresses at all.
	 */
	private static int worstCaseDeflateBound(int length) {
		return length + (length >> 12) + (length >> 14) + (length >> 25) + 13 + 6;
	}

}
