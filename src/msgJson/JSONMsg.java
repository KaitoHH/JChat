package msgJson;

import java.io.UnsupportedEncodingException;

import org.json.JSONObject;

public class JSONMsg {
	public static byte[] toByte(String command, String content) throws UnsupportedEncodingException {
		content = string2Json(content);
		command = string2Json(command);
		String json = String.format("{\"command\":\"%s\",\"content\":\"%s\"}", command, content);
		return json.getBytes("utf-8");
	}

	public static String getCommand(String json) {
		JSONObject jo = new JSONObject(json);
		return jo.getString("command");
	}

	public static String getContent(String json) {
		JSONObject jo = new JSONObject(json);
		String ret = jo.getString("content");
		return ret;
	}

	public static String string2Json(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '/':
					sb.append("\\/");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
}
