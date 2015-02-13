/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */

package me.taylorkelly.mywarp;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import me.taylorkelly.mywarp.dataconnections.DataConnection;
import me.taylorkelly.mywarp.dataconnections.EventConnectionBridge;
import me.taylorkelly.mywarp.dataconnections.MySqlConnection;
import me.taylorkelly.mywarp.dataconnections.SqLiteConnection;
import me.taylorkelly.mywarp.economy.DummyEconomyManager;
import me.taylorkelly.mywarp.economy.EconomyManager;
import me.taylorkelly.mywarp.economy.SimpleEconomyManager;
import me.taylorkelly.mywarp.limits.DummyLimitManager;
import me.taylorkelly.mywarp.limits.LimitManager;
import me.taylorkelly.mywarp.limits.SimpleLimitManager;
import me.taylorkelly.mywarp.safety.CubicLocationSafety;
import me.taylorkelly.mywarp.safety.TeleportService;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.ProfileService;
import me.taylorkelly.mywarp.warp.EventWarpManager;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpSignManager;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point and container for a working MyWarp implementation. <p> An instance of this class holds and manages
 * MyWarp's internal logic. It is initialized with a {@link me.taylorkelly.mywarp.Platform} which provides MyWarp with
 * the needed connection to the game.</p>
 */
public class MyWarp {

  private static final Logger log = Logger.getLogger(MyWarp.class.getName());

  private final Platform platform;
  private final EventWarpManager warpManager;
  private final DataConnection dataConnection;

  private EconomyManager economyManager;
  private LimitManager limitManager;
  private WarpSignManager warpSignManager;

  private TeleportService teleportService;


  /**
   * Creates an instance of MyWarp, running on the given Platform.
   *
   * @param platform the Platform MyWarp runs on
   * @throws InitializationException if the initialization fails and MyWarp is unable to continue
   */
  public MyWarp(Platform platform) throws InitializationException {
    this.platform = platform;

    // setup dataConnection
    ListenableFuture<DataConnection> futureConnection;
    if (getSettings().isMysqlEnabled()) {
      futureConnection =
          MySqlConnection.getConnection(this, getSettings().getMysqlDsn(), getSettings().getMysqlUsername(),
                                        getSettings().getMysqlPassword(), true);
    } else {
      futureConnection = SqLiteConnection.getConnection(this, new File(platform.getDataFolder(), "mywarp.db"), true);
    }
    try {
      dataConnection = futureConnection.get();
    } catch (InterruptedException e) {
      throw new InitializationException("Failed to get a connection to the database as the process was interrupted.",
                                        e);
    } catch (ExecutionException e) {
      throw new InitializationException("Failed to get a connection to the database.", e.getCause());
    }

    // setup the warpManager
    warpManager = new EventWarpManager(new EventConnectionBridge(dataConnection));

    DynamicMessages.setControl(platform.getResourceBundleControl());

    // setup TeleportService
    teleportService = new TeleportService(new CubicLocationSafety(), getSettings());

    // setup the rest of the plugin
    setupPlugin();
  }

  /**
   * Sets up the plugin.
   */
  private void setupPlugin() {
    // setup the limitManager
    if (getSettings().isLimitsEnabled()) {
      limitManager = new SimpleLimitManager(platform.getLimitProvider(), warpManager);
    } else {
      limitManager = new DummyLimitManager(platform.getGame(), warpManager);
    }

    // setup the economyManager
    try {
      if (getSettings().isEconomyEnabled()) {
        economyManager =
            new SimpleEconomyManager(getSettings(), platform.getFeeProvider(), platform.getEconomyService());
      } else {
        economyManager = new DummyEconomyManager();
      }
    } catch (UnsupportedOperationException e) {
      economyManager = new DummyEconomyManager();
    }

    //TODO this might not be needed
    warpSignManager = new WarpSignManager(getSettings().getWarpSignsIdentifiers(), economyManager, warpManager);

    // Populate the warpManager when the warps are loaded. The callback is
    // executed in the server thread.
    log.info("Loading warps..."); // NON-NLS
    Futures.addCallback(getDataConnection().getWarps(), new FutureCallback<Collection<Warp>>() {

      @Override
      public void onSuccess(Collection<Warp> result) {
        warpManager.populate(result);
        log.info(warpManager.getSize() + " warps loaded."); // NON-NLS
      }

      @Override
      public void onFailure(Throwable throwable) {
        log.log(Level.SEVERE, "Failed to load warps from the database.", throwable); // NON-NLS
      }

    }, platform.getGame().getExecutor());

  }

  /**
   * Reloads MyWarp.
   */
  public void reload() {
    // cleanup
    warpManager.clear();
    DynamicMessages.clearCache();

    // setup new stuff
    platform.reload();
    setupPlugin();
  }

  /**
   * Unloads MyWarp. Using this method will effectively shutdown MyWarp. If its called, the platform running MyWarp
   * <b>must</b> unload or deactivate MyWarp too or things will get ugly.
   */
  public void unload() {
    if (dataConnection != null) {
      dataConnection.close();
    }
  }

  /**
   * Gets the TeleportService.
   *
   * @return the TeleportService
   */
  public TeleportService getTeleportService() {
    return teleportService;
  }

  /**
   * Gets the EconomyManager. Calling this method will always return valid EconomyManager implementation, if economy
   * support is disabled on the configuration file, the returned EconomyManager will handle this internally and fail
   * quietly.
   *
   * @return the EconomyManager
   */
  public EconomyManager getEconomyManager() {
    return economyManager;
  }

  /**
   * Gets the LimitManager. Calling this method will always return valid LimitManager implementation, if limit support
   * is disabled on the configuration file, the returned LimitManager will handle this internally and fail quietly.
   *
   * @return the LimitManager
   */
  public LimitManager getLimitManager() {
    return limitManager;
  }

  /**
   * Gets the Platform running MyWarp.
   *
   * @return the Platform
   */
  public Platform getPlatform() {
    return platform;
  }

  /**
   * Gets the DataConnection of this MyWarp instance.
   *
   * @return the DataConnection
   */
  public DataConnection getDataConnection() {
    return dataConnection;
  }

  /**
   * Gets the WarpManager of this MyWarp instance.
   *
   * @return the WarpManager
   */
  public EventWarpManager getWarpManager() {
    return warpManager;
  }

  /**
   * Gets the WarpSignManager instance of this MyWarp instance.
   *
   * @return the WarpSignManager
   */
  public WarpSignManager getWarpSignManager() {
    return warpSignManager;
  }

  public Settings getSettings() {
    return platform.getSettings();
  }

  /**
   * Gets the ProfileService instance of this MyWarp instance.
   *
   * @return the ProfileService
   */
  public ProfileService getProfileService() {
    return platform.getProfileService();
  }

  public Game getGame() {
    return platform.getGame();
  }
}