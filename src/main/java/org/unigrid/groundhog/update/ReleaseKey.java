/*
    Unigrid Groundhog
    Copyright © 2021-2026 Stiftelsen The Unigrid Foundation

    Stiftelsen The Unigrid Foundation (org. nr: 802482-2408)

    This program is free software: you can redistribute it and/or modify it under the terms of the
    GNU General Public License as published by the Free Software Foundation, version 3 of the License
    (see LICENSE).

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
    even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License with this program.
    If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigrid-project/groundhog>.
 */

package org.unigrid.groundhog.update;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

/**
 * The Unigrid Foundation release key, the GPG key that signs every Hedgehog release download. Its public
 * half is the release-key.asc built into the jar.
 */
public class ReleaseKey {
	private static final String RESOURCE = "/release-key.asc";
	private static final int CHUNK = 1 << 16;
	private static final JcaPGPContentVerifierBuilderProvider VERIFIERS =
		new JcaPGPContentVerifierBuilderProvider().setProvider(new BouncyCastleProvider());

	private final PGPPublicKeyRingCollection ring;

	public ReleaseKey() {
		this(Bundled.RING);
	}

	ReleaseKey(final PGPPublicKeyRingCollection ring) {
		this.ring = ring;
	}

	/** True only when one of the signatures in the armored block is a valid one by the release key. */
	public boolean verify(final byte[] data, final byte[] armoredSignature) {
		return verify(() -> new ByteArrayInputStream(data), armoredSignature);
	}

	public boolean verify(final Path data, final byte[] armoredSignature) {
		return verify(() -> Files.newInputStream(data), armoredSignature);
	}

	private boolean verify(final Source data, final byte[] armoredSignature) {
		try {
			final Object packet = new JcaPGPObjectFactory(PGPUtil.getDecoderStream(
				new ByteArrayInputStream(armoredSignature))).nextObject();

			if (!(packet instanceof PGPSignatureList signatures)) {
				return false;
			}

			for (final PGPSignature signature : signatures) {
				final PGPPublicKey key = ring.getPublicKey(signature.getKeyID());

				if (Objects.nonNull(key) && signedBy(signature, key, data)) {
					return true;
				}
			}

			return false;
		} catch (IOException | PGPException e) {
			return false;
		}
	}

	private static boolean signedBy(final PGPSignature signature, final PGPPublicKey key, final Source data)
		throws IOException, PGPException {

		signature.init(VERIFIERS, key);

		try (InputStream in = data.open()) {
			final byte[] chunk = new byte[CHUNK];

			for (int read = in.read(chunk); read >= 0; read = in.read(chunk)) {
				signature.update(chunk, 0, read);
			}
		}

		return signature.verify();
	}

	@FunctionalInterface
	private interface Source {
		InputStream open() throws IOException;
	}

	private static final class Bundled {
		private static final PGPPublicKeyRingCollection RING = load();

		private static PGPPublicKeyRingCollection load() {
			try (InputStream stream = Objects.requireNonNull(
				ReleaseKey.class.getResourceAsStream(RESOURCE), "No " + RESOURCE + " in the build")) {
				return new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(stream),
					new JcaKeyFingerprintCalculator());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (PGPException e) {
				throw new IllegalStateException("The bundled release key cannot be read", e);
			}
		}
	}
}
