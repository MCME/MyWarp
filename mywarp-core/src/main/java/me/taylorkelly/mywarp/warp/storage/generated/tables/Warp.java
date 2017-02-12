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

/**
 * This class is generated by jOOQ
 */
package me.taylorkelly.mywarp.warp.storage.generated.tables;


import me.taylorkelly.mywarp.warp.Warp.Type;
import me.taylorkelly.mywarp.warp.storage.converter.DateTimestampConverter;
import me.taylorkelly.mywarp.warp.storage.converter.TypeConverter;
import me.taylorkelly.mywarp.warp.storage.generated.Keys;
import me.taylorkelly.mywarp.warp.storage.generated.Mywarp;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
import org.jooq.types.UInteger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(value = {"http://www.jooq.org", "jOOQ version:3.6.2"}, comments = "This class is generated by jOOQ")
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Warp extends TableImpl<Record> {

  private static final long serialVersionUID = -641657537;

  /**
   * The reference instance of <code>mywarp.warp</code>
   */
  public static final Warp WARP = new Warp();

  /**
   * The class holding records for this type
   */
  @Override
  public Class<Record> getRecordType() {
    return Record.class;
  }

  /**
   * The column <code>mywarp.warp.warp_id</code>.
   */
  public final TableField<Record, UInteger>
          WARP_ID =
          createField("warp_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.name</code>.
   */
  public final TableField<Record, String>
          NAME =
          createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(32).nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.player_id</code>.
   */
  public final TableField<Record, UInteger>
          PLAYER_ID =
          createField("player_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.x</code>.
   */
  public final TableField<Record, Double>
          X =
          createField("x", org.jooq.impl.SQLDataType.DOUBLE.nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.y</code>.
   */
  public final TableField<Record, Double>
          Y =
          createField("y", org.jooq.impl.SQLDataType.DOUBLE.nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.z</code>.
   */
  public final TableField<Record, Double>
          Z =
          createField("z", org.jooq.impl.SQLDataType.DOUBLE.nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.pitch</code>.
   */
  public final TableField<Record, Float>
          PITCH =
          createField("pitch", org.jooq.impl.SQLDataType.REAL.nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.yaw</code>.
   */
  public final TableField<Record, Float>
          YAW =
          createField("yaw", org.jooq.impl.SQLDataType.REAL.nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.world_id</code>.
   */
  public final TableField<Record, UInteger>
          WORLD_ID =
          createField("world_id", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false), this, "");

  /**
   * The column <code>mywarp.warp.creation_date</code>.
   */
  public final TableField<Record, Date>
          CREATION_DATE =
          createField("creation_date", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "",
                  new DateTimestampConverter());

  /**
   * The column <code>mywarp.warp.type</code>.
   */
  public final TableField<Record, Type>
          TYPE =
          createField("type", org.jooq.impl.SQLDataType.TINYINTUNSIGNED.nullable(false), this, "", new TypeConverter());

  /**
   * The column <code>mywarp.warp.visits</code>.
   */
  public final TableField<Record, UInteger>
          VISITS =
          createField("visits", org.jooq.impl.SQLDataType.INTEGERUNSIGNED.nullable(false).defaulted(true), this, "");

  /**
   * The column <code>mywarp.warp.welcome_message</code>.
   */
  public final TableField<Record, String>
          WELCOME_MESSAGE =
          createField("welcome_message", org.jooq.impl.SQLDataType.CLOB, this, "");

  /**
   * Create a <code>mywarp.warp</code> table reference
   */
  public Warp() {
    this("warp", null);
  }

  /**
   * Create an aliased <code>mywarp.warp</code> table reference
   */
  public Warp(String alias) {
    this(alias, WARP);
  }

  private Warp(String alias, Table<Record> aliased) {
    this(alias, aliased, null);
  }

  private Warp(String alias, Table<Record> aliased, Field<?>[] parameters) {
    super(alias, Mywarp.MYWARP, aliased, parameters, "");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Identity<Record, UInteger> getIdentity() {
    return Keys.IDENTITY_WARP;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UniqueKey<Record> getPrimaryKey() {
    return Keys.KEY_WARP_PRIMARY;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<UniqueKey<Record>> getKeys() {
    return Arrays.<UniqueKey<Record>>asList(Keys.KEY_WARP_PRIMARY, Keys.KEY_WARP_WARP_NAME_UQ);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ForeignKey<Record, ?>> getReferences() {
    return Arrays.<ForeignKey<Record, ?>>asList(Keys.WARP_PLAYER_ID_FK, Keys.WARP_WORLD_ID_FK);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Warp as(String alias) {
    return new Warp(alias, this);
  }

  /**
   * Rename this table
   */
  public Warp rename(String name) {
    return new Warp(name, null);
  }
}
