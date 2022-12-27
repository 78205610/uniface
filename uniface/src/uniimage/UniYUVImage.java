/**
 * 
 */
package uniimage;

/**
 * @author rechard
 *
 */
public class UniYUVImage extends UniImage {
	private static final long serialVersionUID = 1955916483431586374L;

	public static class YUVType extends PixelType {
		private static final long serialVersionUID = 6140423631913044403L;
		
		public final boolean uv; // uv分量排列顺序，true表示uv，false表示vu
		public final int bits; // 平均每像素数据的位宽，有效值为12、16、32
		public final int pn; // 平面数Planar Number，有效值为1、2、3
		public final int yhm; // 当uv分量索引在水平方向+1时，Y分量索引在水平方向的增量倍数，有效值为1或2
		public final int yvm; // 当uv分量索引在垂直方向+1时，Y分量索引在垂直方向的增量倍数，有效值为1或2
		public final int ysi; // y分量在数据区的起始索引
		public final int usi; // u分量在数据区的起始索引
		public final int vsi; // v分量在数据区的起始索引
		public final int ys; // y分量递增步长，有效值为1、4
		public final int uvs; // uv分量递增步长，有效值为1、2、4
		public final int yr;
		public final int yg;
		public final int yb;
		public final int yc;
		public final int ur;
		public final int ug;
		public final int ub;
		public final int uc;
		public final int vr;
		public final int vg;
		public final int vb;
		public final int vc;
		public final int ry;
		public final int ru;
		public final int rv;
		public final int rc;
		public final int gy;
		public final int gu;
		public final int gv;
		public final int gc;
		public final int by;
		public final int bu;
		public final int bv;
		public int bc;
		public YUVType(int bits, int pn, int yhm, int yvm, int ysi, int usi, int vsi, int ys, int uvs, boolean uv, int yr, int yg, int yb, int yc, int ur, int ug, int ub, int uc, int vr, int vg, int vb, int vc, int ry, int ru, int rv, int rc, int gy, int gu, int gv, int gc, int by, int bu, int bv, int bc) {
			super(bits);
			this.bits = bits;
			this.pn = pn; this.yhm = yhm; this.yvm = yvm;
			this.ys = ys; this.uvs = uvs; // y分量和uv分量增长步长
			this.uv = uv;
			this.ysi = ysi; this.usi = usi; this.vsi = vsi; // yuv各分量在数据区的起始
			this.yr = yr; this.yg = yg; this.yb = yb; this.yc = yc;
			this.ur = ur; this.ug = ug; this.ub = ub; this.uc = uc;
			this.vr = vr; this.vg = vg; this.vb = vb; this.vc = vc;
			this.ry = ry; this.ru = ru; this.rv = rv; this.rc = rc;
			this.gy = gy; this.gu = gu; this.gv = gv; this.gc = gc;
			this.by = by; this.bu = bu; this.bv = bv; this.bc = bc;
		}
		public boolean equals(YUVType type) {
			if (this.pn != type.pn || this.yhm != type.yhm || this.yvm != type.yvm)
				return false;
			if (this.ys != type.ys || this.uvs != type.uvs)
				return false;
			if (this.uv != type.uv)
				return false;
			if (this.ysi != type.ysi || this.usi != type.usi || this.vsi != type.vsi)
				return false;
			if (this.yr != type.yr || this.yg != type.yg || this.yb != type.yb || this.yc != type.yc)
				return false;
			if (this.ur != type.ur || this.ug != type.ug || this.ub != type.ub || this.uc != type.uc)
				return false;
			if (this.vr != type.vr || this.vg != type.vg || this.vb != type.vb || this.vc != type.vc)
				return false;
			if (this.ry != type.ry || this.ru != type.ru || this.rv != type.rv || this.rc != type.rc)
				return false;
			if (this.gy != type.gy || this.gu != type.gu || this.gv != type.gv || this.gc != type.gc)
				return false;
			if (this.by != type.by || this.bu != type.bu || this.bv != type.bv || this.bc != type.bc)
				return false;
			return super.equals(type);
		}
	}
	// RGB->full range YUV转换计算整数常量
	// Y = ((77*R + 150*G + 29*B)>>8) + 0;
	public final static int FYR = 77;
	public final static int FYG = 150;
	public final static int FYB = 29;
	public final static int FYC = 0;
	// U = ((-44*R - 87*G + 131*B)>>8) + 128;
	public final static int FUR = -44;
	public final static int FUG = -87;
	public final static int FUB = 131;
	public final static int FUC = 128;
	// V = ((131*R - 110*G - 21*B)>>8) + 128;	
	public final static int FVR = 131;
	public final static int FVG = -110;
	public final static int FVB = -21;
	public final static int FVC = 128;
	// RGB->TV range YUV转换计算整数常量
	// Y = ((66*R + 129*G + 25*B)>>8) + 16;
	public final static int TYR = 66;
	public final static int TYG = 129;
	public final static int TYB = 25;
	public final static int TYC = 16;
	// U = ((-38*R - 74*G + 112*B)>>8) +128;
	public final static int TUR = -38;
	public final static int TUG = -74;
	public final static int TUB = 112;
	public final static int TUC = 128;
	// V = ((112*R - 94*G - 18*B)>>8) + 128;	
	public final static int TVR = 112;
	public final static int TVG = -94;
	public final static int TVB = -18;
	public final static int TVC = 128;
	// full range YUV->RGB转换计算整数常量
	// R = (256*Y + 360*V - 46080)>>8;
	// R = {ry*Y + ru*U + rv*V - rc)>>8;
	public final static int FRY = 256;
	public final static int FRU = 0;
	public final static int FRV = 360;
	public final static int FRC = -46080;
	// G = (256*Y - 88*U - 184*V + 34816)>>8; 
	public final static int FGY = 256;
	public final static int FGU = -88;
	public final static int FGV = -184;
	public final static int FGC = 34816;
	// B = (256*Y + 455*U - 58240)>>8;
	public final static int FBY = 256;
	public final static int FBU = 455;
	public final static int FBV = 0;
	public final static int FBC = -58240;
	// TV range YUV->RGB转换计算整数常量
	// R = (298*Y + 411*V - 57344)>>8;
	public final static int TRY = 298;
	public final static int TRU = 0;
	public final static int TRV = 411;
	public final static int TRC = -57344;
	// G = (298*Y - 101*U - 211*V + 34739)>>8;
	public final static int TGY = 298;
	public final static int TGU = -101;
	public final static int TGV = -211;
	public final static int TGC = 34739;
	// B = (298*Y + 519*U - 71117)>>8;
	public final static int TBY = 298;
	public final static int TBU = 519;
	public final static int TBV = 0;
	public final static int TBC = -71117;

	private YUVType type;
	
	public YUVType getType() {
		return type;
	}
	public void setType(YUVType type) {
		this.type = type;
	}

	public final static YUVType YUV_420_888 =	new YUVType(12, 3, 2, 2, 0, 0, 0, 1, 1, true, FYR, FYG, FYB, FYC, FUR, FUG, FUB, FUC, FVR, FVG, FVB, FVC, FRY, FRU, FRV, FRC, FGY, FGU, FGV, FGC, FBY, FBU, FBV, FBC);
	public final static YUVType NV21 =    		new YUVType(12, 2, 2, 2, 0, 0, 1, 1, 2, true, FYR, FYG, FYB, FYC, FUR, FUG, FUB, FUC, FVR, FVG, FVB, FVC, FRY, FRU, FRV, FRC, FGY, FGU, FGV, FGC, FBY, FBU, FBV, FBC);
	public final static YUVType NV12 =    		new YUVType(12, 2, 2, 2, 0, 1, 0, 1, 2, false, FYR, FYG, FYB, FYC, FUR, FUG, FUB, FUC, FVR, FVG, FVB, FVC, FRY, FRU, FRV, FRC, FGY, FGU, FGV, FGC, FBY, FBU, FBV, FBC);
	public final static YUVType NV21_TV = 		new YUVType(12, 2, 2, 2, 0, 0, 1, 1, 2, true, TYR, TYG, TYB, TYC, TUR, TUG, TUB, TUC, TVR, TVG, TVB, TVC, TRY, TRU, TRV, TRC, TGY, TGU, TGV, TGC, TBY, TBU, TBV, TBC);
	public final static YUVType NV12_TV = 		new YUVType(12, 2, 2, 2, 0, 1, 0, 1, 2, false, TYR, TYG, TYB, TYC, TUR, TUG, TUB, TUC, TVR, TVG, TVB, TVC, TRY, TRU, TRV, TRC, TGY, TGU, TGV, TGC, TBY, TBU, TBV, TBC);
	/**
	 * 
	 */
	public UniYUVImage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 创建一个YUV图片并根据类型分配好图片数据空间
	 * @param type 指定图片类型
	 * @param width 图片像素宽度
	 * @param height 图片像素高度
	 */
	public UniYUVImage(YUVType type, int width, int height) {
		this.type = type;
		this.setWidth(width);
		this.setHeight(height);
		int wh = width * height;
		int len = wh * type.bits / 8;
		this.setImageData(new byte[len]);
	}

	/**
	 * 创建一个YUV图片并直接引用指定的YUV图片数据
	 * @param type 指定图片类型
	 * @param width 图片像素宽度
	 * @param height 图片像素高度
	 * @param imageData YUV图片数据
	 */
	public UniYUVImage(YUVType type, int width, int height, byte[] imageData) {
		super(width, height, imageData);
		this.type = type;
	}

	@Override
	public UniImage create(boolean copyImageData) {
		UniImage img = new UniYUVImage(this.type, this.getWidth(), this.getHeight());
		if (copyImageData) {
			img.setInsideGeometryGraph(UniGeometryGraph.clone(this.getInsideGeometryGraph()));
			img.setDegree(this.getDegree());
			img.setOrientation(this.getOrientation());
			img.setMirror(this.isMirror());
			System.arraycopy(this.getImageData(), 0, img.getImageData(), 0, this.getImageData().length);
		}
		return img;
	}

	@Override
	public UniImage create(int width, int height) {
		return new UniYUVImage(this.type, width, height);
	}
	@Override
	protected PixelType getPixelType() {
		return this.type;
	}

}
