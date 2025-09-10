package com.example.be.commons.util;


import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class StringUtils {
    public static final String LINK_TEXT = ";;;";

    public static String castToLikeRequestParam(String input) {
        return castToLikeRequestParam(input, true);
    }

    public static String castToLikeRequestParam(String input, boolean toLowerCase) {
        if (input == null) return null;
        input = input.trim();
        if (toLowerCase) {
            return input.length() > 0 ? ("%" + input.toLowerCase() + "%") : null;
        }
        return input.length() > 0 ? ("%" + input + "%") : null;
    }

    public static String parseRequestParam(String input) {
        return parseRequestParam(input, true);
    }

    public static String parseRequestParam(String input, boolean toLowerCase) {
        if (input == null) return null;
        input = input.trim();
        if (toLowerCase) {
            return input.length() > 0 ? input.toLowerCase() : null;
        }
        return input.length() > 0 ? input : null;
    }

    public static boolean isNullOrEmpty(final String s) {
        return isNullOrEmpty(s, true);
    }

    public static boolean isNullOrEmpty(final String s, boolean trim) {
        if (s == null || s.isEmpty()) {
            return true;
        }
        return trim && s.trim().isEmpty();
    }

    public static String parseArray(List<String> input) {
        String result = "";
        if (input != null && input.size() > 0) {
            for (String st : input) {
                result = result + st + LINK_TEXT;
            }
            result = result.substring(0, result.length() - LINK_TEXT.length());
        }
        return result;
    }

    public static List<String> toArray(String input) {
        List<String> idList = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(input, true) && input != null) {
            String[] ids = input.split(LINK_TEXT);
            if (ids != null && ids.length > 0) {
                for (String st : ids) {
                    idList.add(st.trim());
                }
            }
        }
        return idList;
    }

    public static List<String> toArrayWithoutTrim(String input) {
        List<String> idList = new ArrayList<>();
        if (!StringUtils.isNullOrEmpty(input, false) && input != null) {
            String[] ids = input.split(LINK_TEXT);
            if (ids != null && ids.length > 0) {
                for (String st : ids) {
                    idList.add(st);
                }
            }
        }
        return idList;
    }

    public static String millisecondToHHMMSS(long millis) {
        StringBuilder result = new StringBuilder();
        if (millis > 1000) {
            long hh = TimeUnit.MILLISECONDS.toHours(millis);
            long mm = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
            long ss = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
            long ms = millis - TimeUnit.HOURS.toMillis(hh) - TimeUnit.MINUTES.toMillis(mm) - TimeUnit.SECONDS.toMillis(ss);
            if (hh > 0) {
                result.append(hh + " h ");
            }
            if (mm > 0) {
                result.append(mm + " m ");
            }
            if (ss > 0) {
                result.append(ss + " s ");
            }
            if (ms > 0) {
                result.append(ms + " ms");
            }
            return result.toString();
        } else {
            return result.append(millis + " ms").toString();
        }
    }

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("");
    }

    public static String genarateNameWithDate(String filename, Date date) {
        if (date == null) date = new Date();
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(date) + filename;
    }

    public static String handleRequestStringDataMongoDB(String value) {
        String newValue = "";
        if (!StringUtils.isNullOrEmpty(value)) {
            String specialChars = "/*!@#$%^&*()\\\"{}_[]|\\\\?/<>,.";
            for (int i = 0; i < value.length(); i++) {
                if (specialChars.contains(value.substring(i, i + 1))) {
                    newValue += "\\" + value.substring(i, i + 1);
                } else {
                    newValue += value.substring(i, i + 1);
                }
            }
        }
        return newValue;
    }

    public static String replaceHTMLTagAndGetByLength(String value, int wordLengths) {
        String result = "";
        if (isNullOrEmpty(value)) return result;
        try {
            String[] words = value.replaceAll("<p>", "").split("</p>");
            String lineAfterRemoveHtmlTag = "";
            for (int i = 0; i < words.length; i++) {
                lineAfterRemoveHtmlTag += words[i];
                if (i < words.length - 1) {
                    lineAfterRemoveHtmlTag += "%new_line%";
                }
            }
            String[] wordSpaces = lineAfterRemoveHtmlTag.split("\\s+");
            String lineWithSpecificWordLength = "";
            for (int i = 0; i < wordSpaces.length; i++) {
                if (i == wordLengths) {
                    lineWithSpecificWordLength += "...";
                    break;
                }
                lineWithSpecificWordLength += wordSpaces[i] + " ";
            }
            result = lineWithSpecificWordLength.replaceAll("%new_line%", "\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

}
