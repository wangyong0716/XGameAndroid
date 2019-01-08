package com.xgame.common.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Ascii.equalsIgnoreCase;

public class TaggedTextParser {

    private static final String TAG_COLOR = "color";

    private static final String TAG_FONT_SIZE = "fontSize";

    private static final String TAG_SIZE = "size";

    private static final String SUP = "sup";

    private static final String TRUE = "true";

    private static final String UNIT_DP = "dp";

    private static final String COLOR = "^#([0-9a-fA-F]{2}){3,4}$";

    private static final Pattern MARKUP_PATTERN =
            Pattern.compile("<span\\s?((\\s+[\\w]+\\s*=\\s*'[^']+'\\s*)+)?>([^<]*)</span>");

    private static final Pattern VALUE_PATTERN = Pattern.compile("([\\w]+)\\s*=\\s*'([^']+)'");

    private static final Pattern TEXT_SIZE_PATTERN = Pattern.compile("([\\d]+)([sd]p)?");

    private static final int IDX_PROPERTY = 1;

    private static final int IDX_NAME = 1;

    private static final int IDX_VALUE = 2;

    private static final int IDX_SIZE_VALUE = 1;

    private static final int IDX_SIZE_UNIT = 2;

    private static final int EXTRA_PADDING = 2;

    public static boolean isTaggedTextWithFuzzy(String text) {
        return text != null && text.contains("</span>");
    }

    public static Spannable parseTaggedText(Context ctx, String text) {
        List<TaggedText> taggedList = parseText(text);
        if (taggedList != null && taggedList.size() > 0) {
            SpannableStringBuilder ss = new SpannableStringBuilder();
            appendTextWithoutTag(ctx, text, taggedList, ss);
            return ss;
        }
        return new SpannableString(TextUtils.isEmpty(text) ? "" : text);
    }

    public static void setTaggedText(final TextView textView, String text) {
        if (textView == null) {
            return;
        }
        CharSequence message = text;
        if (!TextUtils.isEmpty(message)) {
            Spannable spans = parseTaggedText(textView.getContext(), text);
            if (spans != null) {
                SuperscriptSpan[] sa = spans.getSpans(0, spans.length(), SuperscriptSpan.class);
                if (sa != null && sa.length > 0) {
                    textView.setPadding(0, EXTRA_PADDING, 0, 0);
                }
                message = spans;
            }
        }
        textView.setText(message);
    }

    private static List<TaggedText> parseText(String text) {
        Matcher m = MARKUP_PATTERN.matcher(text);
        List<TaggedText> list = new ArrayList<TaggedText>();
        while (m.find()) {
            TaggedText tagged = new TaggedText();
            tagged.start = m.start();
            tagged.end = m.end();
            tagged.content = m.group(m.groupCount());
            list.add(tagged);
            String property = m.group(IDX_PROPERTY);
            if (property == null) {
                continue;
            }
            Matcher pm = VALUE_PATTERN.matcher(property);
            while (pm.find()) {
                tagged.tagValues.put(pm.group(IDX_NAME), pm.group(IDX_VALUE));
            }
        }
        return list;
    }

    private static void appendTextWithoutTag(Context ctx, String text, List<TaggedText> taggedList,
            SpannableStringBuilder ss) {
        int start = 0;
        for (TaggedText tagged : taggedList) {
            if (start < tagged.start) {
                ss.append(text.substring(start, tagged.start));
            }
            int offset = ss.length();
            ss.append(tagged.content);
            setSpan(ctx, tagged, ss, offset, ss.length());
            start = tagged.end;
        }
        ss.append(text.substring(start, text.length()));
    }

    private static void setSpan(Context ctx, TaggedText tagged, Spannable sp, int start, int end) {
        TextAppearanceSpan span = null;
        boolean isSup = false;
        for (Map.Entry<String, String> entry : tagged.tagValues.entrySet()) {
            String tag = entry.getKey();
            String value = entry.getValue();
            if (equalsIgnoreCase(tag, TAG_COLOR) && isStringColor(value)) {
                span = new TextAppearanceSpan(null, 0, 0,
                        ColorStateList.valueOf(Color.parseColor(value)), null);
            } else if (equalsIgnoreCase(tag, TAG_FONT_SIZE) || equalsIgnoreCase(tag, TAG_SIZE)) {
                Matcher m = TEXT_SIZE_PATTERN.matcher(value);
                if (m.find()) {
                    String textSize = m.group(IDX_SIZE_VALUE);
                    String textUnit = m.group(IDX_SIZE_UNIT);
                    int unit = equalsIgnoreCase(textUnit, UNIT_DP) ? TypedValue.COMPLEX_UNIT_DIP
                            : TypedValue.COMPLEX_UNIT_SP;
                    float px = TypedValue
                            .applyDimension(unit, Integer.valueOf(textSize),
                                    ctx.getResources().getDisplayMetrics());
                    span = new TextAppearanceSpan(null, 0, (int) px, null, null);
                }
            } else if (equalsIgnoreCase(tag, SUP)) {
                if (equalsIgnoreCase(value, TRUE)) {
                    isSup = true;
                }
            }
            if (span != null) {
                sp.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        if (isSup) {
            sp.setSpan(new SuperscriptSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static boolean isStringColor(String value) {
        return Pattern.matches(COLOR, value);
    }

    private static class TaggedText {

        public int start;

        public int end;

        public String content;

        Map<String, String> tagValues = new HashMap<String, String>();

        @Override
        public String toString() {
            return "TaggedText{" +
                    "start=" + start +
                    ", end=" + end +
                    ", content='" + content + '\'' +
                    ", tagValues=" + tagValues +
                    '}';
        }
    }

}
