package com.quickblox.q_municate.ui.chats.emoji.emojiTypes;

import java.io.Serializable;

public class EmojiObject implements Serializable {

    private static final long serialVersionUID = 1L;
    private int icon;
    private char value;
    private String emoji;

    private EmojiObject() {
    }

    public EmojiObject(String emoji) {
        this.emoji = emoji;
    }

    public static EmojiObject fromResource(int icon, int value) {
        EmojiObject emoji = new EmojiObject();
        emoji.icon = icon;
        emoji.value = (char) value;
        return emoji;
    }

    public static EmojiObject fromCodePoint(int codePoint) {
        EmojiObject emoji = new EmojiObject();
        emoji.emoji = newString(codePoint);
        return emoji;
    }

    public static EmojiObject fromChar(char ch) {
        EmojiObject emoji = new EmojiObject();
        emoji.emoji = Character.toString(ch);
        return emoji;
    }

    public static EmojiObject fromChars(String chars) {
        EmojiObject emoji = new EmojiObject();
        emoji.emoji = chars;
        return emoji;
    }

    public static final String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }

    public char getValue() {
        return value;
    }

    public int getIcon() {
        return icon;
    }

    public String getEmoji() {
        return emoji;
    }
}