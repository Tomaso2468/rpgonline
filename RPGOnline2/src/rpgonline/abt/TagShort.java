package rpgonline.abt;

public class TagShort extends Tag {
	/**
	 * ID for serilization.
	 */
	private static final long serialVersionUID = -2634062576907254468L;
	private short data;
	
	public TagShort(String name, short data) {
		super(name, 0x03);
	}
	
	public short getData() {
		return data;
	}
	
	public void setData(short data) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("{\n");

		appendStr(sb, "name", getName());
		appendSep(sb);
		appendNum(sb, "type", getType());
		appendSep(sb);
		appendNum(sb, "data", data);
		
		sb.append("\n}");

		return sb.toString();
	}
	
	@Override
	public TagShort clone() {
		return new TagShort(name, getData());
	}

}
