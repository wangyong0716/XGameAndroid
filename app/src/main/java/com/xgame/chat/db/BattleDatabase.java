package com.xgame.chat.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = GameBattle.class, version = 1, exportSchema = false)
public abstract class BattleDatabase extends RoomDatabase {

    public abstract BattleDao battleDao();

}
