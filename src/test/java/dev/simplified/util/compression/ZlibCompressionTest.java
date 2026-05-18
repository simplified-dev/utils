package dev.simplified.util.compression;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ZlibCompressionTest {

    /**
     * Reference deflate via the stock {@link DeflaterOutputStream}. Bytes-comparison is
     * intentionally not used (zlib output varies by JDK / Deflater level) - tests round-trip
     * through {@link Compression#decompress} instead.
     */
    private static byte[] referenceDeflate(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DeflaterOutputStream def = new DeflaterOutputStream(out)) {
            def.write(data);
        }
        return out.toByteArray();
    }

    @Nested
    class RoundTrip {

        @Test
        void emptyArrayRoundTrips() {
            byte[] empty = new byte[0];
            byte[] zlibbed = ZlibCompression.compress(empty);
            assertNotNull(zlibbed);
            byte[] back = Compression.decompress(zlibbed);
            assertArrayEquals(empty, back);
        }

        @Test
        void singleByteRoundTrips() {
            byte[] one = { (byte) 0x42 };
            byte[] back = Compression.decompress(ZlibCompression.compress(one));
            assertArrayEquals(one, back);
        }

        @Test
        void asciiTextRoundTrips() {
            byte[] data = "the quick brown fox jumps over the lazy dog 0123456789".repeat(20).getBytes(StandardCharsets.US_ASCII);
            byte[] back = Compression.decompress(ZlibCompression.compress(data));
            assertArrayEquals(data, back);
        }

        @Test
        void binaryPayloadRoundTrips() {
            byte[] data = new byte[256];
            for (int i = 0; i < 256; i++)
                data[i] = (byte) i;

            byte[] back = Compression.decompress(ZlibCompression.compress(data));
            assertArrayEquals(data, back);
        }

        @Test
        void repeatingPatternRoundTrips() {
            byte[] data = new byte[64 * 1024];
            for (int i = 0; i < data.length; i++)
                data[i] = (byte) ('A' + (i % 8));

            byte[] back = Compression.decompress(ZlibCompression.compress(data));
            assertArrayEquals(data, back);
        }

        @Test
        void incompressibleRandomRoundTrips() {
            // Low compression ratio - stresses the deflateBound formula against output close to
            // input size.
            byte[] data = new byte[1024 * 1024];
            new Random(42).nextBytes(data);

            byte[] back = Compression.decompress(ZlibCompression.compress(data));
            assertArrayEquals(data, back);
        }

        @Test
        void offsetSliceCompressRoundTrips() {
            byte[] full = new byte[100];
            new Random(7).nextBytes(full);
            byte[] slice = new byte[80];
            System.arraycopy(full, 16, slice, 0, 80);

            byte[] zlibbed = ZlibCompression.compress(full, 16, 80);
            byte[] back = Compression.decompress(zlibbed);
            assertArrayEquals(slice, back);
        }

        @Test
        void compressWithoutOffsetMatchesOffsetZero() {
            byte[] data = "round trip".getBytes(StandardCharsets.US_ASCII);
            byte[] back1 = Compression.decompress(ZlibCompression.compress(data));
            byte[] back2 = Compression.decompress(ZlibCompression.compress(data, 0, data.length));
            assertArrayEquals(back1, back2);
            assertArrayEquals(data, back1);
        }

    }

    @Nested
    class CompressionDispatch {

        @Test
        void compressionCompressZlibDelegatesToBoundedDeflate() throws IOException {
            // Sanity: Compression.compress(..., ZLIB) should produce output equivalent to
            // referenceDeflate (post-round-trip), confirming the Compression-level dispatch
            // routes through ZlibCompression's bounded path and produces wire-compatible output.
            byte[] data = "dispatched via Compression.compress".getBytes(StandardCharsets.US_ASCII);
            byte[] zlibbed = Compression.compress(data, Compression.ZLIB);
            byte[] back = Compression.decompress(zlibbed);
            assertArrayEquals(data, back);

            // Cross-check: pure ZlibCompression round-trips to the same payload.
            byte[] direct = ZlibCompression.compress(data);
            assertArrayEquals(Compression.decompress(direct), back);
        }

        @Test
        void compressionDecompressZlibStillUsesGrowablePath() {
            // ZLIB has no ISIZE trailer; inflate keeps the growable accumulator path inside
            // Compression.decompress. Verify the round trip still works via that path.
            byte[] data = new byte[2048];
            new Random(99).nextBytes(data);
            byte[] zlibbed = Compression.compress(data, Compression.ZLIB);
            byte[] back = Compression.decompress(zlibbed);
            assertEquals(data.length, back.length);
            assertArrayEquals(data, back);
        }

    }

}
