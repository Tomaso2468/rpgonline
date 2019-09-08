package io.github.tomaso2468.rpgonline.abt;

@Deprecated
public class TagVector4 extends TagDoubleArray {
	/**
	 * ID for serilization.
	 */
	private static final long serialVersionUID = 4087268138371073020L;

	public TagVector4(String name, double a, double b, double c, double d) {
		super(name, new double[] {a, b, c, d});
	}
	
	@Override
	public byte getType() {
		return 0x1C;
	}
	
	@Override
	public TagVector4 clone() {
		return new TagVector4(name, getData()[0], getData()[1], getData()[2], getData()[3]);
	}
}