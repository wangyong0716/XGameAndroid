package com.xgame.chat;

import com.xgame.chat.db.GameBattle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public class ChatMessage {

    public static final int TYPE_GAME   = 1;
    public static final int TYPE_TIME   = 2;
    public static final int TYPE_EVENT   = 3;

    public int type;

    // for time tpye or event tpye
    public String text;

    // for type game
    public GameBattle gameBattle;

    public ChatMessage(GameBattle gameBattle) {
        this.type = TYPE_GAME;
        this.gameBattle = gameBattle;
    }

    public ChatMessage() {
    }

    public ChatMessage(long time) {
        this.type = TYPE_TIME;
        text = ChatDateFormatUtil.getFormatTextForTime(time);
    }

    public ChatMessage(String eventString) {
        type = TYPE_EVENT;
        text = eventString;
    }

}
