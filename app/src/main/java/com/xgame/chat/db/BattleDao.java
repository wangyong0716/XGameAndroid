package com.xgame.chat.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface BattleDao {

    @Insert
    public long addBattle(GameBattle battle);

    @Insert
    public long[] addBattles(List<GameBattle> battle);

    @Update
    public void updateBattles(GameBattle... battles);

    @Update
    public void updateBattles(List<GameBattle> battles);

    @Query("SELECT * FROM battle WHERE opponent_id = :opponent AND create_time < :lastTime ORDER BY create_time DESC LIMIT 10")
    public List<GameBattle> getBattles(long opponent, long lastTime);

    @Query("SELECT * FROM battle WHERE opponent_id = :opponent ORDER BY create_time DESC LIMIT 10")
    public List<GameBattle> getBattles(long opponent);

    @Query("SELECT * FROM battle WHERE opponent_id = :opponent AND session_id = :sessionId")
    public GameBattle getBattle(long opponent, String sessionId);

    @Query("DELETE FROM battle")
    public void deleteAllDatas();
}
