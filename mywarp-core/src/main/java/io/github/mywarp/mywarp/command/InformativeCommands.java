/*
 * Copyright (C) 2011 - 2017, MyWarp team and contributors
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

package io.github.mywarp.mywarp.command;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Ordering;
import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.OptArg;
import com.sk89q.intake.parametric.annotation.Range;
import com.sk89q.intake.parametric.annotation.Switch;
import com.sk89q.intake.util.auth.AuthorizationException;

import io.github.mywarp.mywarp.command.parametric.annotation.Billable;
import io.github.mywarp.mywarp.command.parametric.annotation.Viewable;
import io.github.mywarp.mywarp.command.parametric.namespace.IllegalCommandSenderException;
import io.github.mywarp.mywarp.command.util.CommandUtil;
import io.github.mywarp.mywarp.command.util.paginator.StringPaginator;
import io.github.mywarp.mywarp.command.util.printer.AssetsPrinter;
import io.github.mywarp.mywarp.command.util.printer.InfoPrinter;
import io.github.mywarp.mywarp.platform.Actor;
import io.github.mywarp.mywarp.platform.Game;
import io.github.mywarp.mywarp.platform.LocalEntity;
import io.github.mywarp.mywarp.platform.LocalPlayer;
import io.github.mywarp.mywarp.platform.LocalWorld;
import io.github.mywarp.mywarp.platform.PlayerNameResolver;
import io.github.mywarp.mywarp.service.economy.FeeType;
import io.github.mywarp.mywarp.service.limit.LimitService;
import io.github.mywarp.mywarp.util.Message;
import io.github.mywarp.mywarp.util.i18n.DynamicMessages;
import io.github.mywarp.mywarp.warp.Warp;
import io.github.mywarp.mywarp.warp.WarpManager;
import io.github.mywarp.mywarp.warp.authorization.AuthorizationResolver;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * Bundles commands that provide information about existing Warps.
 */
public final class InformativeCommands {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final AuthorizationResolver authorizationResolver;
  private final WarpManager warpManager;
  private final Game game;
  private final PlayerNameResolver playerNameResolver;
  @Nullable
  private final LimitService limitService;

  /**
   * Creates an instance.
   *
   * @param warpManager           the WarpManager used by commands
   * @param limitService          the LimitService used by commands - may be {@code null} if no limit service is used
   * @param authorizationResolver the AuthorizationResolver used by commands
   * @param game                  the Game used by commands
   * @param playerNameResolver    the PlayerNameResolver used by commands
   */
  InformativeCommands(WarpManager warpManager, @Nullable LimitService limitService,
                      AuthorizationResolver authorizationResolver, Game game, PlayerNameResolver playerNameResolver) {
    this.authorizationResolver = authorizationResolver;
    this.warpManager = warpManager;
    this.game = game;
    this.limitService = limitService;
    this.playerNameResolver = playerNameResolver;
  }

  @Command(aliases = {"assets", "limits"}, desc = "assets.description", help = "assets.help")
  @Require("mywarp.cmd.assets.self")
  @Billable(FeeType.ASSETS)
  public void assets(Actor actor, @OptArg LocalPlayer creator)
      throws IllegalCommandSenderException, AuthorizationException {
    if (creator == null) {
      if (actor instanceof LocalPlayer) {
        creator = (LocalPlayer) actor;
      } else {
        throw new IllegalCommandSenderException(actor);
      }
    } else if (!actor.hasPermission("mywarp.cmd.assets")) {
      throw new AuthorizationException();
    }

    AssetsPrinter printer;
    if (limitService != null) {
      printer = AssetsPrinter.create(creator, limitService);
    } else {
      printer = AssetsPrinter.create(creator, game, warpManager);
    }
    printer.print(actor);
  }

  @Command(aliases = {"list", "alist"}, desc = "list.description", help = "list.help")
  @Require("mywarp.cmd.list")
  @Billable(FeeType.LIST)
  public void list(final Actor actor, @OptArg("1") int page, @Switch('c') final String creator,
                   @Switch('n') final String name,
                   @Switch('r') @Range(min = 1, max = Integer.MAX_VALUE) final Integer radius,
                   @Switch('w') final String world) throws IllegalCommandSenderException {

    // build the listing predicate
    Predicate<Warp> filter = authorizationResolver.isViewable(actor);

    if (creator != null) {
      filter = filter.and(input -> {
        Optional<String> creatorName = playerNameResolver.getByUniqueId(input.getCreator());
        return creatorName.isPresent() && StringUtils.containsIgnoreCase(creatorName.get(), creator);
      });
    }

    if (name != null) {
      filter = filter.and(input -> StringUtils.containsIgnoreCase(input.getName(), name));
    }

    if (radius != null) {
      if (!(actor instanceof LocalEntity)) {
        throw new IllegalCommandSenderException(actor);
      }

      LocalEntity entity = (LocalEntity) actor;

      final UUID worldId = entity.getWorld().getUniqueId();

      final int squaredRadius = radius * radius;
      final Vector3d position = entity.getPosition();
      filter = filter.and(input -> input.getWorldIdentifier().equals(worldId)
                                   && input.getPosition().distanceSquared(position) <= squaredRadius);
    }

    if (world != null) {
      filter = filter.and(input -> {
        Optional<LocalWorld> worldOptional = game.getWorld(input.getWorldIdentifier());
        return worldOptional.isPresent() && StringUtils.containsIgnoreCase(worldOptional.get().getName(), world);
      });
    }

    //query the warps
    //noinspection RedundantTypeArguments
    final List<Warp> warps = Ordering.natural().sortedCopy(warpManager.getAll(filter));

    Function<Warp, Message> mapping = input -> {
      // 'name' (world) by player
      Message.Builder builder = Message.builder();
      builder.append("'");
      builder.append(input);
      builder.append("' (");
      builder.append(CommandUtil.toWorldName(input.getWorldIdentifier(), game));
      builder.append(") ");
      builder.append(msg.getString("list.by"));
      builder.append(" ");

      if (actor instanceof LocalPlayer && input.isCreator(((LocalPlayer) actor).getUniqueId())) {
        builder.append(msg.getString("list.you"));
      } else {
        builder.append(CommandUtil.toName(input.getCreator(), playerNameResolver));
      }
      return builder.build();
    };

    // display
    StringPaginator.of(msg.getString("list.heading"), warps).withMapping(mapping::apply).paginate()
        .display(actor, page);
  }

  @Command(aliases = {"info", "stats"}, desc = "info.description", help = "info.help")
  @Require("mywarp.cmd.info")
  @Billable(FeeType.INFO)
  public void info(Actor actor, @Viewable Warp warp) {
    new InfoPrinter(warp, authorizationResolver, game, playerNameResolver).print(actor);
  }
}
