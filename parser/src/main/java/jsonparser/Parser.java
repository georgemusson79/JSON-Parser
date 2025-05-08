package jsonparser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Parser {

    public ArrayList<Pair<Token, Object>> tokenise(String JSONString) throws Exception {
        ArrayList<Pair<Token, Object>> tokens = new ArrayList<>();
        int i = 0;
        while (i < JSONString.length()) {
            char ch = JSONString.charAt(i);
            switch (ch) {
            case '{':
                tokens.add(new Pair<>(Token.LBRACE, "{"));
                break;
            case '}':
                tokens.add(new Pair<>(Token.RBRACE, "}"));
                break;
            case '"':
                int oldI = i;
                i = JSONString.indexOf(ch, i + 1);
                if (i == -1) throw new Exception("Unable to find closing speech mark");
                String str = JSONString.substring(oldI + 1, i);
                tokens.add(new Pair<>(Token.STRING, str));
                break;
            case ',':
                tokens.add(new Pair<>(Token.COMMA, ","));
                break;
            case ':':
                tokens.add(new Pair<>(Token.COLON, ":"));
                break;
            case 't':
                if (JSONString.startsWith("true", i)) {
                    tokens.add(new Pair<>(Token.BOOL, true));
                    i += 3;
                }
                else {
                    throw new Exception("Unexpected token starting with 't'");
                }
                break;
            case 'f':
                if (JSONString.startsWith("false", i)) {
                    tokens.add(new Pair<>(Token.BOOL, false));
                    i += 4;
                }
                else {
                    throw new Exception("Unexpected token starting with 'f'");
                }
                break;

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
                String strnum = "";
                strnum += ch;
                boolean decimalAdded = false;
                char nextChar = JSONString.charAt(i + 1);
                if (ch == '-') {
                    if (!Character.isDigit(nextChar)) throw new Exception("Invalid Token");
                }

                while (Character.isDigit(nextChar) || nextChar == '.') {
                    if (nextChar == '.') {
                        if (decimalAdded) throw new Exception("Malformed number at " + i);
                        decimalAdded = true;
                    }
                    strnum += nextChar;
                    i++;
                    nextChar = JSONString.charAt(i + 1);

                }

                double num = Double.parseDouble(strnum);
                tokens.add(new Pair<>(Token.NUMBER, num));

                break;

            case '[':
                tokens.add(new Pair<Token, Object>(Token.LSQUARE_BRACKET, "["));
                break;
            case ']':
                tokens.add(new Pair<Token, Object>(Token.RSQUARE_BRACKET, "]"));
                break;

            }

            i++;
        }

        return tokens;
    }

    public HashMap<String, Object> parseJSONString(String JSONString) throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        ArrayList<Pair<Token, Object>> tokens = this.tokenise(JSONString);
        this.parseObject(tokens);
        return result;

    }

    public void assertToken(Token t, ArrayList<Pair<Token, Object>> tokens, int pos) throws Exception {
        if (tokens.get(pos).first != t) throw new Exception("invalid token: expected " + t.toString() + "but got " + tokens.get(pos).first.toString());

    }

    // return array length in chars and arrayList
    public Pair<Integer, ArrayList<Object>> parseArray(ArrayList<Pair<Token, Object>> tokens, int pos) throws Exception {
        this.assertToken(Token.LSQUARE_BRACKET, tokens, pos);
        ArrayList<Object> list = new ArrayList<>();
        pos++;
        Token nextToken = tokens.get(pos).first;

        if (nextToken == Token.RSQUARE_BRACKET) {
            pos++;
            return new Pair<Integer, ArrayList<Object>>(pos, list);
        }
        while (true) {

            nextToken = tokens.get(pos).first;
            Object value = tokens.get(pos).second;
            if (nextToken == Token.NUMBER || nextToken == Token.STRING || nextToken == Token.BOOL) {
                pos++;
                list.add(value);
            }

            else if (nextToken == Token.LSQUARE_BRACKET) {
                Pair<Integer, ArrayList<Object>> arrPair = this.parseArray(tokens, pos);
                list.add(arrPair.second);
                pos = arrPair.first;
            }

            else if (nextToken == Token.LBRACE) {
                ArrayList<Pair<Token, Object>> newTokens = new ArrayList<>(tokens.subList(pos, tokens.size()));
                Pair<Integer, HashMap<String, Object>> pair = this.parseObject(newTokens);
                pos += pair.first;
                list.add(pair.second);
            }

            else
                throw new Exception("Invalid Token");

            nextToken = tokens.get(pos).first;
            value = tokens.get(pos).second;
            if (nextToken == Token.RSQUARE_BRACKET) {
                pos++;
                break;
            }

            else if (nextToken == Token.COMMA) {
                pos++;
            }
            else {
                throw new Exception("Invalid Token");
            }

        }

        return new Pair<Integer, ArrayList<Object>>(pos, list);

    }

    // return length and object
    public Pair<Integer, HashMap<String, Object>> parseObject(ArrayList<Pair<Token, Object>> tokens) throws Exception {
        HashMap<String, Object> output = new HashMap<>();
        int pos = 0;
        this.assertToken(Token.LBRACE, tokens, pos);
        pos++;
        if (tokens.get(pos).first == Token.RBRACE) return new Pair<Integer, HashMap<String, Object>>(2, output);

        // parse key value pairs
        while (true) {
            String key;
            Object value;
            this.assertToken(Token.STRING, tokens, pos);
            key = (String) tokens.get(pos).second;
            pos++;
            this.assertToken(Token.COLON, tokens, pos);
            pos++;
            Token nextToken = tokens.get(pos).first;
            if (nextToken == Token.BOOL || nextToken == Token.NUMBER || nextToken == Token.STRING) {
                value = tokens.get(pos).second;
                pos++;
            }
            else if (nextToken == Token.LSQUARE_BRACKET) {
                Pair<Integer, ArrayList<Object>> res = parseArray(tokens, pos);
                ArrayList<Object> arr = res.second;
                pos = res.first;
                value = arr;
            }

            else if (nextToken == Token.LBRACE) {
                ArrayList<Pair<Token, Object>> newTokens = new ArrayList<>(tokens.subList(pos, tokens.size()));
                Pair<Integer, HashMap<String, Object>> pair = this.parseObject(newTokens);
                pos += pair.first;
                value = pair.second;
            }

            else {
                throw new Exception("Unexpected token");
            }

            output.put(key, value);

            nextToken = tokens.get(pos).first;
            if (nextToken == Token.RBRACE) {
                pos++;
                break;
            }
            else {
                this.assertToken(Token.COMMA, tokens, pos);
                pos++;
            }

        }

        return new Pair<Integer, HashMap<String, Object>>(pos, output);
    }
}
