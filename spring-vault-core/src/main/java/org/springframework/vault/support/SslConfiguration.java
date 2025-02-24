/*
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.vault.support;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Arrays;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * SSL configuration.
 * <p>
 * Provides configuration for a key store and trust store for TLS certificate
 * verification. Key store and trust store may be left unconfigured if the JDK trust store
 * contains all necessary certificates to verify TLS certificates. The key store is used
 * for Client Certificate authentication.
 *
 * @author Mark Paluch
 * @see Resource
 * @see java.security.KeyStore
 * @see org.springframework.vault.authentication.ClientCertificateAuthentication
 */
public class SslConfiguration {

	private static final String DEFAULT_KEYSTORE_TYPE = KeyStore.getDefaultType();

	private final KeyStoreConfiguration keyStoreConfiguration;

	private final KeyStoreConfiguration trustStoreConfiguration;

	private final KeyConfiguration keyConfiguration;

	/**
	 * Create a new {@link SslConfiguration} with the default {@link KeyStore} type.
	 *
	 * @param keyStore the key store resource, must not be {@literal null}.
	 * @param keyStorePassword the key store password.
	 * @param trustStore the trust store resource, must not be {@literal null}.
	 * @param trustStorePassword the trust store password.
	 * @deprecated Since 1.1, use
	 * {@link #SslConfiguration(KeyStoreConfiguration, KeyStoreConfiguration)} to prevent
	 * {@link String} interning and retaining passwords represented as String longer from
	 * GC than necessary.
	 */
	@Deprecated
	public SslConfiguration(Resource keyStore, @Nullable String keyStorePassword,
			Resource trustStore, @Nullable String trustStorePassword) {

		this(new KeyStoreConfiguration(keyStore, charsOrNull(keyStorePassword),
				DEFAULT_KEYSTORE_TYPE),
				new KeyStoreConfiguration(trustStore, charsOrNull(trustStorePassword),
						DEFAULT_KEYSTORE_TYPE));
	}

	/**
	 * Create a new {@link SslConfiguration}.
	 *
	 * @param keyStoreConfiguration the key store configuration, must not be
	 *     {@literal null}.
	 * @param trustStoreConfiguration the trust store configuration, must not be
	 *     {@literal null}.
	 * @since 1.1
	 */
	public SslConfiguration(KeyStoreConfiguration keyStoreConfiguration,
			KeyStoreConfiguration trustStoreConfiguration) {
		this(keyStoreConfiguration, KeyConfiguration.unconfigured(),
				trustStoreConfiguration);
	}

	/**
	 * Create a new {@link SslConfiguration}.
	 *
	 * @param keyStoreConfiguration the key store configuration, must not be
	 *     {@literal null}.
	 * @param keyConfiguration the configuration for a specific key in
	 *     {@code keyStoreConfiguration} to use.
	 * @param trustStoreConfiguration the trust store configuration, must not be
	 *     {@literal null}.
	 * @since 2.2
	 */
	public SslConfiguration(KeyStoreConfiguration keyStoreConfiguration,
			KeyConfiguration keyConfiguration,
			KeyStoreConfiguration trustStoreConfiguration) {

		Assert.notNull(keyStoreConfiguration, "KeyStore configuration must not be null");
		Assert.notNull(keyConfiguration, "KeyConfiguration must not be null");
		Assert.notNull(trustStoreConfiguration,
				"TrustStore configuration must not be null");

		this.keyStoreConfiguration = keyStoreConfiguration;
		this.keyConfiguration = keyConfiguration;
		this.trustStoreConfiguration = trustStoreConfiguration;
	}

	/**
	 * Create a new {@link SslConfiguration} for the given trust store with the default
	 * {@link KeyStore} type.
	 *
	 * @param trustStore resource pointing to an existing trust store, must not be
	 *     {@literal null}.
	 * @param trustStorePassword may be {@literal null}.
	 * @return the created {@link SslConfiguration}.
	 * @see java.security.KeyStore
	 * @deprecated Since 1.1, use {@link #forTrustStore(Resource, char[])} to prevent
	 * {@link String} interning and retaining passwords represented as String longer from
	 * GC than necessary.
	 */
	@Deprecated
	public static SslConfiguration forTrustStore(Resource trustStore,
			@Nullable String trustStorePassword) {
		return forTrustStore(trustStore, charsOrNull(trustStorePassword));
	}

	/**
	 * Create a new {@link SslConfiguration} for the given trust store with the default
	 * {@link KeyStore} type.
	 *
	 * @param trustStore resource pointing to an existing trust store, must not be
	 *     {@literal null}.
	 * @param trustStorePassword may be {@literal null}.
	 * @return the created {@link SslConfiguration}.
	 * @see java.security.KeyStore
	 */
	public static SslConfiguration forTrustStore(Resource trustStore,
			@Nullable char[] trustStorePassword) {

		Assert.notNull(trustStore, "TrustStore must not be null");
		Assert.isTrue(trustStore.exists(),
				() -> String.format("TrustStore %s does not exist", trustStore));

		return new SslConfiguration(KeyStoreConfiguration.unconfigured(),
				KeyConfiguration.unconfigured(), new KeyStoreConfiguration(trustStore,
						trustStorePassword, DEFAULT_KEYSTORE_TYPE));
	}

	/**
	 * Create a new {@link SslConfiguration} for the given {@link KeyStoreConfiguration
	 * trust store}.
	 *
	 * @param trustStore must not be {@literal null}.
	 * @return a new {@link SslConfiguration} with {@link KeyStoreConfiguration trust
	 * store configuration} applied.
	 * @since 2.2
	 * @see java.security.KeyStore
	 */
	public static SslConfiguration forTrustStore(KeyStoreConfiguration trustStore) {
		return unconfigured().withTrustStore(trustStore);
	}

	/**
	 * Create a new {@link SslConfiguration} for the given key store with the default
	 * {@link KeyStore} type.
	 *
	 * @param keyStore resource pointing to an existing key store, must not be
	 *     {@literal null}.
	 * @param keyStorePassword may be {@literal null}.
	 * @return the created {@link SslConfiguration}.
	 * @see java.security.KeyStore
	 * @deprecated Since 1.1, use {@link #forKeyStore(Resource, char[])} to prevent
	 * {@link String} interning and retaining passwords represented as String longer from
	 * GC than necessary.
	 */
	@Deprecated
	public static SslConfiguration forKeyStore(Resource keyStore,
			@Nullable String keyStorePassword) {
		return forKeyStore(keyStore, charsOrNull(keyStorePassword));
	}

	/**
	 * Create a new {@link SslConfiguration} for the given key store with the default
	 * {@link KeyStore} type.
	 *
	 * @param keyStore resource pointing to an existing key store, must not be
	 *     {@literal null}.
	 * @param keyStorePassword may be {@literal null}.
	 * @return the created {@link SslConfiguration}.
	 * @see java.security.KeyStore
	 */
	public static SslConfiguration forKeyStore(Resource keyStore,
			@Nullable char[] keyStorePassword) {
		return forKeyStore(new KeyStoreConfiguration(keyStore, keyStorePassword,
				DEFAULT_KEYSTORE_TYPE), KeyConfiguration.unconfigured());
	}

	/**
	 * Create a new {@link SslConfiguration} for the given {@link KeyStoreConfiguration
	 * key store}.
	 *
	 * @param keyStore resource pointing to an existing key store, must not be
	 *     {@literal null}.
	 * @return the created {@link SslConfiguration}.
	 * @since 2.2
	 * @see java.security.KeyStore
	 */
	public static SslConfiguration forKeyStore(KeyStoreConfiguration keyStore) {
		return forKeyStore(keyStore, KeyConfiguration.unconfigured());
	}

	/**
	 * Create a new {@link SslConfiguration} for the given {@link KeyStoreConfiguration
	 * key store} and {@link KeyConfiguration}.
	 *
	 * @param keyStore resource pointing to an existing key store, must not be
	 *     {@literal null}.
	 * @param keyConfiguration the configuration for a specific key in
	 *     {@code keyStoreConfiguration} to use.
	 * @return the created {@link SslConfiguration}.
	 * @since 2.2
	 * @see java.security.KeyStore
	 */
	public static SslConfiguration forKeyStore(KeyStoreConfiguration keyStore,
			KeyConfiguration keyConfiguration) {
		return unconfigured().withKeyStore(keyStore, keyConfiguration);
	}

	/**
	 * Create a new {@link SslConfiguration} for the given key store with the default
	 * {@link KeyStore} type.
	 *
	 * @param keyStore resource pointing to an existing key store, must not be
	 *     {@literal null}.
	 * @param keyStorePassword may be {@literal null}.
	 * @param keyConfiguration the configuration for a specific key in
	 *     {@code keyStoreConfiguration} to use.
	 * @return the created {@link SslConfiguration}.
	 * @since 2.2
	 * @see java.security.KeyStore
	 */
	public static SslConfiguration forKeyStore(Resource keyStore,
			@Nullable char[] keyStorePassword, KeyConfiguration keyConfiguration) {

		Assert.notNull(keyStore, "KeyStore must not be null");
		Assert.isTrue(keyStore.exists(),
				() -> String.format("KeyStore %s does not exist", keyStore));
		Assert.notNull(keyConfiguration, "KeyConfiguration must not be null");

		return new SslConfiguration(
				new KeyStoreConfiguration(keyStore, keyStorePassword,
						DEFAULT_KEYSTORE_TYPE),
				keyConfiguration, KeyStoreConfiguration.unconfigured());
	}

	/**
	 * Create a new {@link SslConfiguration} for the given truststore with the default
	 * {@link KeyStore} type.
	 *
	 * @param keyStore resource pointing to an existing keystore, must not be
	 *     {@literal null}.
	 * @param keyStorePassword may be {@literal null}.
	 * @param trustStore resource pointing to an existing trust store, must not be
	 *     {@literal null}.
	 * @param trustStorePassword may be {@literal null}.
	 * @return the created {@link SslConfiguration}.
	 * @see java.security.KeyStore
	 * @deprecated Since 1.1, use {@link #create(Resource, char[], Resource, char[])} to
	 * prevent {@link String} interning and retaining passwords represented as String
	 * longer from GC than necessary.
	 */
	@Deprecated
	public SslConfiguration create(Resource keyStore, @Nullable String keyStorePassword,
			Resource trustStore, @Nullable String trustStorePassword) {
		return create(keyStore, charsOrNull(keyStorePassword), trustStore,
				charsOrNull(trustStorePassword));
	}

	/**
	 * Create a new {@link SslConfiguration} for the given truststore with the default
	 * {@link KeyStore} type.
	 *
	 * @param keyStore resource pointing to an existing keystore, must not be
	 *     {@literal null}.
	 * @param keyStorePassword may be {@literal null}.
	 * @param trustStore resource pointing to an existing trust store, must not be
	 *     {@literal null}.
	 * @param trustStorePassword may be {@literal null}.
	 * @return the created {@link SslConfiguration}.
	 * @see java.security.KeyStore
	 */
	public static SslConfiguration create(Resource keyStore,
			@Nullable char[] keyStorePassword, Resource trustStore,
			@Nullable char[] trustStorePassword) {

		Assert.notNull(keyStore, "KeyStore must not be null");
		Assert.isTrue(keyStore.exists(),
				() -> String.format("KeyStore %s does not exist", trustStore));

		Assert.notNull(trustStore, "TrustStore must not be null");
		Assert.isTrue(trustStore.exists(),
				String.format("TrustStore %s does not exist", trustStore));

		return new SslConfiguration(
				new KeyStoreConfiguration(keyStore, keyStorePassword,
						DEFAULT_KEYSTORE_TYPE),
				new KeyStoreConfiguration(trustStore, trustStorePassword,
						DEFAULT_KEYSTORE_TYPE));
	}

	/**
	 * Factory method returning an unconfigured {@link SslConfiguration} instance.
	 *
	 * @return an unconfigured {@link SslConfiguration} instance.
	 * @since 2.0
	 */
	public static SslConfiguration unconfigured() {
		return new SslConfiguration(KeyStoreConfiguration.unconfigured(),
				KeyStoreConfiguration.unconfigured());
	}

	/**
	 * @return the {@link java.security.KeyStore key store} resource or {@literal null} if
	 * not configured.
	 */
	public Resource getKeyStore() {
		return keyStoreConfiguration.getResource();
	}

	/**
	 * @return the key store password or {@literal null} if not configured.
	 * @deprecated Since 1.1, use {@link KeyStoreConfiguration#getStorePassword()} to
	 * prevent {@link String} interning and retaining passwords represented as String
	 * longer from GC than necessary.
	 */
	@Deprecated
	@Nullable
	public String getKeyStorePassword() {
		return stringOrNull(keyStoreConfiguration.getStorePassword());
	}

	/**
	 * @return the key store configuration.
	 * @since 1.1
	 */
	public KeyStoreConfiguration getKeyStoreConfiguration() {
		return keyStoreConfiguration;
	}

	/**
	 * @return the key configuration.
	 * @since 2.2
	 */
	public KeyConfiguration getKeyConfiguration() {
		return keyConfiguration;
	}

	/**
	 * Create a new {@link SslConfiguration} with {@link KeyStoreConfiguration} applied
	 * retaining the {@link #getTrustStoreConfiguration() trust store} configuration.
	 *
	 * @param configuration must not be {@literal null}.
	 * @return a new {@link SslConfiguration} with {@link KeyStoreConfiguration} applied.
	 * @since 2.0
	 */
	public SslConfiguration withKeyStore(KeyStoreConfiguration configuration) {
		return withKeyStore(configuration, KeyConfiguration.unconfigured());
	}

	/**
	 * Create a new {@link SslConfiguration} with {@link KeyStoreConfiguration} and
	 * {@link KeyConfiguration} applied retaining the {@link #getTrustStoreConfiguration()
	 * trust store} configuration.
	 *
	 * @param configuration must not be {@literal null}.
	 * @param keyConfiguration the configuration for a specific key in
	 *     {@code keyStoreConfiguration} to use.
	 * @return a new {@link SslConfiguration} with {@link KeyStoreConfiguration} and
	 * {@link KeyConfiguration} applied.
	 * @since 2.2
	 */
	public SslConfiguration withKeyStore(KeyStoreConfiguration configuration,
			KeyConfiguration keyConfiguration) {
		return new SslConfiguration(configuration, keyConfiguration,
				this.trustStoreConfiguration);
	}

	/**
	 * @return the {@link java.security.KeyStore key store} resource or {@literal null} if
	 * not configured.
	 */
	public Resource getTrustStore() {
		return trustStoreConfiguration.getResource();
	}

	/**
	 * @return the trust store password or {@literal null} if not configured.
	 * @deprecated Since 1.1, use {@link KeyStoreConfiguration#getStorePassword()} to
	 * prevent {@link String} interning and retaining passwords represented as String
	 * longer from GC than necessary.
	 */
	@Deprecated
	@Nullable
	public String getTrustStorePassword() {
		return stringOrNull(trustStoreConfiguration.getStorePassword());
	}

	/**
	 * @return the trust store configuration.
	 * @since 1.1
	 */
	public KeyStoreConfiguration getTrustStoreConfiguration() {
		return trustStoreConfiguration;
	}

	/**
	 * Create a new {@link SslConfiguration} with {@link KeyStoreConfiguration trust store
	 * configuration} applied retaining the {@link #getKeyStoreConfiguration()} key store}
	 * configuration.
	 *
	 * @param configuration must not be {@literal null}.
	 * @return a new {@link SslConfiguration} with {@link KeyStoreConfiguration trust
	 * store configuration} applied.
	 * @since 2.0
	 */
	public SslConfiguration withTrustStore(KeyStoreConfiguration configuration) {
		return new SslConfiguration(this.keyStoreConfiguration, this.keyConfiguration,
				configuration);
	}

	@Nullable
	private static String stringOrNull(@Nullable char[] storePassword) {
		return storePassword != null ? new String(storePassword) : null;
	}

	@Nullable
	private static char[] charsOrNull(@Nullable String trustStorePassword) {
		return trustStorePassword != null ? trustStorePassword.toCharArray() : null;
	}

	/**
	 * Configuration for a key store/trust store.
	 *
	 * @since 1.1
	 */
	public static class KeyStoreConfiguration {

		private static final KeyStoreConfiguration UNCONFIGURED = new KeyStoreConfiguration(
				AbsentResource.INSTANCE, null, DEFAULT_KEYSTORE_TYPE);

		/**
		 * Store that holds certificates, private keys.
		 */
		private final Resource resource;

		/**
		 * Password used to access the key store/trust store.
		 */
		@Nullable
		private final char[] storePassword;

		/**
		 * Key store/trust store type.
		 */
		private final String storeType;

		/**
		 * Create a new {@link KeyStoreConfiguration}.
		 */
		public KeyStoreConfiguration(Resource resource, @Nullable char[] storePassword,
				String storeType) {

			Assert.notNull(resource, "Resource must not be null");
			Assert.isTrue(resource instanceof AbsentResource || resource.exists(),
					() -> String.format("Resource %s does not exist", resource));
			Assert.notNull(storeType, "Keystore type must not be null");

			this.resource = resource;
			this.storeType = storeType;

			if (storePassword == null) {
				this.storePassword = null;
			}
			else {
				this.storePassword = Arrays.copyOf(storePassword, storePassword.length);
			}
		}

		/**
		 * Create a new {@link KeyStoreConfiguration} given {@link Resource}.
		 *
		 * @param resource resource referencing the key store, must not be {@literal null}
		 *     .
		 * @return the {@link KeyStoreConfiguration} for {@code resource}.
		 * @since 2.0
		 */
		public static KeyStoreConfiguration of(Resource resource) {
			return new KeyStoreConfiguration(resource, null, DEFAULT_KEYSTORE_TYPE);
		}

		/**
		 * Create a new {@link KeyStoreConfiguration} given {@link Resource} and
		 * {@code storePassword} using the default keystore type.
		 *
		 * @param resource resource referencing the key store, must not be {@literal null}
		 *     .
		 * @param storePassword key store password, may be {@literal null}.
		 * @return the {@link KeyStoreConfiguration} for {@code resource}.
		 * @since 2.0
		 */
		public static KeyStoreConfiguration of(Resource resource,
				@Nullable char[] storePassword) {
			return new KeyStoreConfiguration(resource, storePassword,
					DEFAULT_KEYSTORE_TYPE);
		}

		/**
		 * Create an unconfigured, empty {@link KeyStoreConfiguration}.
		 *
		 * @return unconfigured, empty {@link KeyStoreConfiguration}.
		 * @since 2.0
		 */
		public static KeyStoreConfiguration unconfigured() {
			return UNCONFIGURED;
		}

		/**
		 * @return {@literal true} if the resource is present.
		 * @since 2.0
		 */
		public boolean isPresent() {
			return !(resource instanceof AbsentResource);
		}

		/**
		 * @return the {@link java.security.KeyStore key store} resource or
		 * {@literal null} if not configured.
		 */
		public Resource getResource() {
			return resource;
		}

		/**
		 * @return the key store/trust store password. Empty {@code char} array if not
		 * set.
		 */
		@Nullable
		public char[] getStorePassword() {
			return storePassword;
		}

		/**
		 * @return the trust store type.
		 */
		public String getStoreType() {
			return storeType;
		}
	}

	/**
	 * Configuration for a key in a keystore.
	 *
	 * @author Mark Paluch
	 * @since 2.2
	 */
	public static class KeyConfiguration {

		private static final KeyConfiguration UNCONFIGURED = new KeyConfiguration(null,
				null);

		private final @Nullable char[] keyPassword;

		private final @Nullable String keyAlias;

		private KeyConfiguration(@Nullable char[] keyPassword,
				@Nullable String keyAlias) {

			if (keyPassword == null) {
				this.keyPassword = null;
			}
			else {
				this.keyPassword = Arrays.copyOf(keyPassword, keyPassword.length);
			}

			this.keyAlias = keyAlias;
		}

		/**
		 * Create an unconfigured, empty {@link KeyConfiguration}.
		 *
		 * @return unconfigured, empty {@link KeyConfiguration}.
		 */
		public static KeyConfiguration unconfigured() {
			return UNCONFIGURED;
		}

		/**
		 * Create a {@link KeyConfiguration} to configure a specific key within a
		 * {@link KeyStore}.
		 *
		 * @param keyPassword the key password to use. Uses
		 *     {@link KeyStoreConfiguration#getStorePassword()} if left {@code null}.
		 * @param keyAlias the key alias to use. Uses the first alias if left {@code null}
		 *     .
		 * @return the {@link KeyConfiguration}.
		 */
		public static KeyConfiguration of(@Nullable char[] keyPassword,
				@Nullable String keyAlias) {
			return new KeyConfiguration(keyPassword, keyAlias);
		}

		/**
		 * @return the key password to use.
		 */
		@Nullable
		public char[] getKeyPassword() {
			return keyPassword;
		}

		/**
		 * @return key alias to use.
		 */
		@Nullable
		public String getKeyAlias() {
			return keyAlias;
		}
	}

	static class AbsentResource extends AbstractResource {

		static final AbsentResource INSTANCE = new AbsentResource();

		private AbsentResource() {
		}

		@Override
		public String getDescription() {
			return getClass().getSimpleName();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			throw new UnsupportedOperationException("Empty resource");
		}
	}
}
