/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ultreon.gameprovider.craft;

import net.fabricmc.api.EnvType;
import org.quiltmc.loader.impl.game.LibClassifier.LibraryType;

enum GameLibrary implements LibraryType {
	ULTREONCRAFT_DESKTOP("com/ultreon/craft/DesktopLauncher.class"),
	ULTREONCRAFT_CORE("com/ultreon/craft/UltreonCraft.class"),
	LIBGDX("com/badlogic/gdx/Gdx.class"),
	LOG4J_API("org/apache/logging/log4j/LogManager.class"),
	LOG4J_CORE("META-INF/services/org.apache.logging.log4j.spi.Provider", "META-INF/log4j-provider.properties"),
	LOG4J_CONFIG("log4j2.xml"),
	GSON("com/google/gson/TypeAdapter.class"), // used by log4j plugins
	SLF4J_API("org/slf4j/Logger.class"),
	SLF4J_CORE("META-INF/services/org.slf4j.spi.SLF4JServiceProvider");

	static final GameLibrary[] GAME = {GameLibrary.ULTREONCRAFT_DESKTOP, GameLibrary.ULTREONCRAFT_CORE};
	static final GameLibrary[] LOGGING = {GameLibrary.LOG4J_API, GameLibrary.LOG4J_CORE, GameLibrary.LOG4J_CONFIG, GameLibrary.GSON, GameLibrary.SLF4J_API, GameLibrary.SLF4J_CORE};

	private final EnvType env;
	private final String[] paths;

	GameLibrary(String path) {
		this(null, new String[] { path });
	}

	GameLibrary(String... paths) {
		this(null, paths);
	}

	GameLibrary(EnvType env, String... paths) {
		this.paths = paths;
		this.env = env;
	}

	@Override
	public boolean isApplicable(EnvType env) {
		return this.env == null || this.env == env;
	}

	@Override
	public String[] getPaths() {
		return paths;
	}
}
