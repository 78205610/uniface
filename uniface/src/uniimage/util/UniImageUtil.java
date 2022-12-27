package uniimage.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import uniimage.UniGeometryGraph;
import uniimage.UniGrayImage;
import uniimage.UniGrayYImage;
import uniimage.UniImage;
import uniimage.UniPoint;
import uniimage.UniRGBImage;
import uniimage.UniRGBImage.RGBType;
import uniimage.UniRect;
import uniimage.UniRectPoints;
import uniimage.UniSize;
import uniimage.UniYUVImage;
import uniimage.UniYUVImage.YUVType;

public class UniImageUtil {
	public static boolean mirror(UniRGBImage srcImage, UniRGBImage mirrorImage, boolean horizontal, boolean vertical) {
		if ((horizontal || vertical) && srcImage.getWidth() == mirrorImage.getWidth() && srcImage.getHeight() == mirrorImage.getHeight() && srcImage.getType().equals(mirrorImage.getType())) {
			int w = srcImage.getWidth();
			int wc = w / 2;
			int h = srcImage.getHeight();
			int hc = h / 2;
			int c = srcImage.getType().bits / 8;
			int lbs = w * c;
			int lbsc = lbs / 2;
			byte[] p1 = new byte[c];
			byte[] p2 = new byte[c];
			byte[] sd = srcImage.getImageData();
			int si = 0;
			byte[] dd = mirrorImage.getImageData();
			int di = 0;
			if (!vertical) {
				// 仅水平镜像
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < lbsc; x += c) {
						// p1为前点，p2为p1镜像点
						for (int i = 0; i < c; i++) {
							int l = si + x + i;
							int r = si + (lbs - x - c) + i;
							p1[i] = sd[si + x + i];
							p2[i] = sd[si + (lbs - x - c) + i];
						}
						for (int i = 0; i < c; i++) {
							dd[di + x + i] = p2[i];
							dd[di + (lbs - x - c) + i] = p1[i];
						}
					}
					si += lbs;
					di += lbs;
				}
			}
			return true;
		}
		return false;
	}
    /**
     * 计算两个浮点向量之间的欧式距离
     * 两个浮点数组的长度必须一致
     * @param vector1
     * @param vector2
     * @return 欧式距离
     */
    public static float euclideanDistance(float[] vector1, float[] vector2) {
        float sumOfSquares = 0.0f;
        for (int i = 0; i < vector1.length; i++) {
            float v = vector1[i] - vector2[i];
            v *= v;
            sumOfSquares += v;
        }
        return (float)Math.sqrt(sumOfSquares);
    }

    /**
     * 计算两个浮点向量之间的余弦距离
     * @param vector1
     * @param vector2
     * @return 余弦距离
     */
    public static float cosDistance(float[] vector1, float[] vector2) {
        float s = 0;
        float s1 = 0;
        float s2 = 0;
        for (int i = 0; i < vector1.length; i++) {
            s += vector1[i] * vector2[i];
            s1 += vector1[i] * vector1[i];
            s2 += vector2[i] * vector2[i];
        }
        double S1 = Math.sqrt(s1);
        double S2 = Math.sqrt(s2);
        double S = s / (S1 * S2);
        return (float)S;
    }
	/**
	 * 将一个字节数组的数值类型转换为一个浮点数组
	 * RGB图片数据归一化例：bytesCaseFloats(imageBytes, imageFloats, 255.0f, null);
	 * @param bytes 字节数组
	 * @param floats 浮点数组，长度必须与字节数组一致。
	 * @param scalefactor 如果指定了，则每一个浮点数都会除以scalefactor，用来在转换过程中完成归一化
	 * @param mean 如果指定了，则每一个浮点数都会减去mean[n]，n在0-mean.lenght之间循环
	 */
	public static void bytesCaseFloats(byte[] bytes, float[] floats, Float scalefactor, float[] mean) {
		int mi = 0;
        for (int i = 0; i < bytes.length; i++) {
            floats[i] = (float)(((int)bytes[i]) & 0xFF);
            if (mean != null) {
            	floats[i] -= mean[mi++];
            	if (mi >= mean.length) {
            		mi = 0;
            	}
            }
            if (scalefactor != null) {
            	floats[i] /= scalefactor.floatValue();
            }
        }
	}
	/**
	 * 将一个字节数组的数值类型转换为一个浮点数组
	 * RGB图片数据归一化例：bytesCaseFloats(imageBytes, imageFloats, 255.0f, null);
	 * @param bytes 字节数组
	 * @param floats 浮点数组，长度必须与字节数组一致。
	 * @param scalefactor 如果指定了，则每一个浮点数都会除以scalefactor，用来在转换过程中完成归一化
	 * @param mean 如果指定了，则每一个浮点数都会减去mean[n]，n在0-mean.lenght之间循环
	 * @param floatsOffsets 浮点数组分组的起始位置(opencv图片处理时，将RGB三个分量分成三个图层，指定三层数据在floats中的起始位置可以在数值转换过程中完成张量形状转换)
	 */
	public static void bytesCaseFloats(byte[] bytes, float[] floats, Float scalefactor, float[] mean, int[] floatsOffsets) {
		int mi = 0;
		int foi = 0;
		int[] fsos = floatsOffsets.clone();
        for (int i = 0; i < bytes.length; i++) {
        	int di = fsos[foi];
            floats[di] = (float)(((int)bytes[i]) & 0xFF);
            if (mean != null) {
            	floats[di] -= mean[mi++];
            	if (mi >= mean.length) {
            		mi = 0;
            	}
            }
            if (scalefactor != null) {
            	floats[di] /= scalefactor.floatValue();
            }
            foi++;
            if (foi >= fsos.length) {
            	for (int j = 0; j < fsos.length; j++) {
            		fsos[j]++;
            	}
            	foi = 0;
            }
        }
	}
	/**
	 * 将一个字节数组的数值类型转换为浮点格式并存入另一个字节数组
	 * RGB图片数据归一化例：bytesCaseFloats(imageBytes, inputData, 255.0f, null);
	 * @param bytes 字节数组
	 * @param floatBytes 用来存储浮点数据的字节数组，长度必须是原字节数组的4倍。
	 * @param scalefactor 如果指定了，则每一个浮点数都会除以scalefactor，用来在转换过程中完成归一化
	 * @param mean 如果指定了，则每一个浮点数都会减去mean[n]，n在0-mean.lenght之间循环
	 */
	public static void bytesCaseFloats(byte[] bytes, byte[] floatBytes, Float scalefactor, float[] mean) {
		int mi = 0;
        for (int i = 0, si = 0; i < floatBytes.length; i += 4, si++) {
            float fv = (float)(((int)bytes[si]) & 0xFF);
            if (mean != null) {
            	fv -= mean[mi++];
            	if (mi >= mean.length) {
            		mi = 0;
            	}
            }
            if (scalefactor != null) {
            	fv /= scalefactor.floatValue();
            }
    		int fbit = Float.floatToIntBits(fv);
    		floatBytes[i + 0] = (byte)(fbit & 0xFF);
    		fbit >>= 8;
    		floatBytes[i + 1] = (byte)(fbit & 0xFF);
			fbit >>= 8;
			floatBytes[i + 2] = (byte)(fbit & 0xFF);
			fbit >>= 8;
			floatBytes[i + 3] = (byte)(fbit & 0xFF);
			fbit >>= 8;
        }
	}
	/**
	 * 将一个浮点数组转化为字节数组
	 * 转换过程不改变浮点数组的内容，只是将浮点数组转换成可供持久化的格式
	 * @param floats 待转换的浮点数组
	 * @param bytes 转换后的字节数据数据，长度必须是浮点数组的4倍
	 */
	public static void floatsToBytes(float[] floats, byte[] bytes) {
    	for (int i = 0, fi = 0; i < bytes.length; i += 4, fi++) {
    		int fbit = Float.floatToIntBits(floats[fi]);
    		bytes[i + 0] = (byte)(fbit & 0xFF);
    		fbit >>= 8;
			bytes[i + 1] = (byte)(fbit & 0xFF);
			fbit >>= 8;
			bytes[i + 2] = (byte)(fbit & 0xFF);
			fbit >>= 8;
			bytes[i + 3] = (byte)(fbit & 0xFF);
			fbit >>= 8;
    	}
	}
    /**
     * 将一个字节数组还原为浮点数组
     * @param bytes 带还原的字节数组，长度必须是4的整数倍
     * @param floats 还原以后的浮点数组，长度必须是字节数组的1/4。
     */
    public static void floatsFromBytes(byte[] bytes, float[] floats) {
        for (int i = 0, fi = 0; i < bytes.length; i += 4, fi++) {
            int fbit = 0;
            fbit = fbit | (bytes[i + 0] & 0xff) << 0;
            fbit = fbit | (bytes[i + 1] & 0xff) << 8;
            fbit = fbit | (bytes[i + 2] & 0xff) << 16;
            fbit = fbit | (bytes[i + 3] & 0xff) << 24;
            floats[fi] = Float.intBitsToFloat(fbit);
        }
    }
	/**
	 * 计算两个矩形的重叠率
	 * @param rect 矩形
	 * @param other 矩形
	 * @return 重叠率，取值范围：0.0-1.0
	 */
	public static float overlapRate(UniRect rect, UniRect other) {
		int sx1 = rect.x;
		int sy1 = rect.y;
		int ex1 = rect.x + rect.width - 1;
		int ey1 = rect.y + rect.height - 1;
		int sx2 = other.x;
		int sy2 = other.y;
		int ex2 = other.x + other.width - 1;
		int ey2 = other.y + other.height - 1;
		int sxo = sx1 > sx2 ? sx1 : sx2;
		int syo = sy1 > sy2 ? sy1 : sy2;
		int exo = ex1 < ex2 ? ex1 : ex2;
		int eyo = ey1 < ey2 ? ey1 : ey2;
		if (sxo > exo || syo > eyo) {
			return 0.0f;
		}
		if (sxo == sx1 && syo == sy1 && exo == ex1 && eyo == ey1) {
			return 1.0f;
		}
		return (float)(exo - sxo + 1) * (eyo - syo + 1) / (float)(rect.width * rect.height);
	}
	/**
	 * 图片缩放
	 * @param image 原图
	 * @param size 缩放尺寸
	 * @return 缩放后的图片
	 */
	public static UniRGBImage resizeImage(UniRGBImage image, UniSize size) {
		if (size.width == image.getWidth() && size.height == image.getHeight()) {
			return (UniRGBImage) image.create(true);
		}
		UniRGBImage img = (UniRGBImage) image.create(size.width, size.height);
		resizeImage(image, img);
		return img;
	}
	/**
	 * 图片缩放
	 * @param imgSrc 原图
	 * @param imgResize 缩放以后的图
	 */
	public static void resizeImage(UniRGBImage imgSrc, UniRGBImage imgResize) {
		imgResize.setInsideGeometryGraph(UniGeometryGraph.resize(UniGeometryGraph.clone(imgSrc.getInsideGeometryGraph()), imgSrc.getWidth(), imgSrc.getHeight(), imgResize.getWidth(), imgResize.getHeight()));
		imgResize.setDegree(imgSrc.getDegree());
		imgResize.setOrientation(imgSrc.getOrientation());
		imgResize.setMirror(imgSrc.isMirror());
		if (imgResize.getWidth() == imgSrc.getWidth() && imgResize.getHeight() == imgSrc.getHeight()) {
			System.arraycopy(imgSrc.getImageData(), 0, imgResize.getImageData(), 0, imgResize.getImageData().length);
			return;
		}
		int pixelByteNum = imgSrc.pixelBits() / 8;
		int srcLineByteNum = pixelByteNum * imgSrc.getWidth();
		UniSize imgSize = new UniSize(imgResize.getWidth(), imgResize.getHeight());
		int srcOffset = 0;
		int var = 0;
		for (int y = 0; y < imgResize.getHeight(); y++) {
			directDrawLine(imgResize.getImageData(), 0, imgSize, pixelByteNum, new UniPoint(0, y), new UniPoint(imgResize.getWidth() - 1, y), imgSrc.getImageData(), srcOffset, srcLineByteNum, null);
			var += imgSrc.getHeight();
			while (var >= imgResize.getHeight()) {
				var -= imgResize.getHeight();
				srcOffset += srcLineByteNum;
			}
		}
	}
	/**
	 * 正方化图片
	 * @param image 原图
	 * @return 正方化以后的图片，边长取原图中的长边。
	 */
	public static UniRGBImage squareImage(UniRGBImage image) {
		return squareImage(image, null);
	}
	/**
	 * 正方化图片
	 * @param image 原图
	 * @param fillGray 正方化产生的空区的灰度填充色，如果指定null则表示不填充。
	 * @return 正方化以后的图片，边长取原图中的长边。
	 */
	public static UniRGBImage squareImage(UniRGBImage image, Byte fillGray) {
		if (image.getWidth() == image.getHeight()) {
			return (UniRGBImage) image.create(true);
		}
		int l = image.getWidth() > image.getHeight() ? image.getWidth() : image.getHeight();
		UniRect rect = new UniRect(0, 0, l, l);
		if (image.getWidth() > image.getHeight()) {
			rect.y = (image.getHeight() - image.getWidth()) / 2;
		} else {
			rect.x = (image.getWidth() - image.getHeight()) / 2;
		}
		return cutImage(image, rect, fillGray);
	}
	/**
	 * 裁剪图片
	 * @param image 原图
	 * @param rect 原图中待裁剪的区域
	 * @return 裁剪出来的图片，尺寸与rect相同。
	 */
	public static UniRGBImage cutImage(UniRGBImage image, UniRect rect) {
		return cutImage(image, rect, null);
	}
	/**
	 * 裁剪图片
	 * @param image 原图
	 * @param rect 原图中待裁剪的区域
	 * @param fillGray 域外裁剪空区的填充灰度，如果指定null则表示不填充。
	 * @return 裁剪出来的图片，尺寸与rect相同。
	 */
	public static UniRGBImage cutImage(UniRGBImage image, UniRect rect, Byte fillGray) {
		UniRGBImage img = (UniRGBImage) image.create(rect.width, rect.height);
		cutImage(image, img, rect.leftTop(), fillGray);
		return img;
	}
	/**
	 * 裁剪图片
	 * @param imgSrc 原图
	 * @param imgCut 裁剪图，裁剪尺寸由裁剪图决定
	 * @param leftTop 裁剪区域左上角在原图中的位置
	 * @param fillGray 域外裁剪空区的填充灰度，如果指定null则表示不填充。
	 */
	public static void cutImage(UniRGBImage imgSrc, UniRGBImage imgCut, UniPoint leftTop, Byte fillGray) {
		if (fillGray != null) {
			Arrays.fill(imgCut.getImageData(), fillGray);
		}
		imgCut.setInsideGeometryGraph(UniGeometryGraph.translate(UniGeometryGraph.clone(imgSrc.getInsideGeometryGraph()), -leftTop.x, -leftTop.y));
		imgCut.setDegree(imgSrc.getDegree());
		imgCut.setOrientation(imgSrc.getOrientation());
		imgCut.setMirror(imgSrc.isMirror());
		int sw = imgSrc.getWidth();
		int sh = imgSrc.getHeight();
		int dw = imgCut.getWidth();
		int dh = imgCut.getHeight();
		int sbx = leftTop.x < 0 ? 0 : leftTop.x; // 原图中实际可裁剪区域的起始X
		int sby = leftTop.y < 0 ? 0 : leftTop.y; // 原图中实际可裁剪区域的起始Y
		int sex = leftTop.x + dw - 1;
		int sey = leftTop.y + dh - 1;
		int dbx = leftTop.x < 0 ? -leftTop.x : 0;
		int dby = leftTop.y < 0 ? -leftTop.y : 0;
		int cw = sex > sw - 1 ? sw - sbx : sex - sbx + 1; // 实际可裁剪的像素宽
		int ch = sey > sh - 1 ? sh - sby : sey - sby + 1; // 实际可裁剪的像素高
		int srcOffset = 0;
		int desOffset = 0;
		if (cw > 0 && ch > 0) {
			int pixelBytes = imgSrc.pixelBits() / 8;
			int si = (sby * sw + sbx) * pixelBytes;
			int slbn = sw * pixelBytes;
			int di = (dby * dw + dbx) * pixelBytes;
			int dlbn = dw * pixelBytes;
			int clbn = cw * pixelBytes;
			byte[] sd = imgSrc.getImageData();
			byte[] dd = imgCut.getImageData();
			for (int i = 0; i < ch; i++) {
				System.arraycopy(sd, srcOffset + si, dd, desOffset + di, clbn);
				si += slbn;
				di += dlbn;
			}
		}
	}
	/**
	 * 裁剪YUV图片
	 * @param image 原图
	 * @param rect 原图中待裁剪的区域
	 * @return 裁剪出来的图片，尺寸与rect相同。
	 */
	public static UniYUVImage cutImage(UniYUVImage image, UniRect rect) {
		UniRect r = new UniRect(rect.x, rect.y, rect.width, rect.height);
		if (image.getType().yhm == 2) {
			r.x = (r.x + 1) & (~1);
			r.width = (r.width + 1) & (~1);
		}
		if (image.getType().yvm == 2) {
			r.y = (r.y + 1) & (~1);
			r.height = (r.height + 1) & (~1);
		}
		UniYUVImage img = (UniYUVImage) image.create(r.width, r.height);
		img.setInsideGeometryGraph(UniGeometryGraph.translate(UniGeometryGraph.clone(image.getInsideGeometryGraph()), -r.x, -r.y));
		img.setDegree(image.getDegree());
		img.setOrientation(image.getOrientation());
		img.setMirror(image.isMirror());
		int srcOffset = 0;
		int desOffset = 0;
		int sw = image.getWidth();
		int sh = image.getHeight();
		int dw = img.getWidth();
		int dh = img.getHeight();
		int sbx = r.x < 0 ? 0 : r.x; // 原图中实际可裁剪区域的起始X
		int sby = r.y < 0 ? 0 : r.y; // 原图中实际可裁剪区域的起始Y
		int sex = r.x + dw - 1; // 原图中裁剪区域的起终止X
		int sey = r.y + dh - 1; // 原图中裁剪区域的起终止Y
		int dbx = r.x < 0 ? - r.x : 0;
		int dby = r.y < 0 ? - r.y : 0;
		int cw = sex > sw - 1 ? sw - sbx : sex - sbx + 1; // 实际可裁剪的像素宽
		int ch = sey > sh - 1 ? sh - sby : sey - sby + 1; // 实际可裁剪的像素高
//		if (image.getType().yhm == 2) {
//			cw &= ~1;
//		}
//		if (image.getType().yvm == 2) {
//			ch &= ~1;
//		}
		if (cw > 0 && ch > 0) {
			int pixelBytes = image.getType().ys;
			int si = (sby * sw + sbx) * pixelBytes;
			int slbn = sw * pixelBytes;
			int di = (dby * dw + dbx) * pixelBytes;
			int dlbn = dw * pixelBytes;
			int clbn = cw * pixelBytes;
			byte[] sd = image.getImageData();
			byte[] dd = img.getImageData();
			for (int i = 0; i < ch; i++) {
				System.arraycopy(sd, srcOffset + si, dd, desOffset + di, clbn);
				si += slbn;
				di += dlbn;
			}
			if (image.getType().pn > 1) {
				// Y分量独立存储
				srcOffset += sw * sh;
				desOffset += dw * dh;
				sw /= image.getType().yhm;
				sh /= image.getType().yvm;
				dw /= image.getType().yhm;
				dh /= image.getType().yvm;
				r.x /= image.getType().yhm;
				r.y /= image.getType().yvm;
				sbx = r.x < 0 ? 0 : r.x; // 原图中实际可裁剪区域的起始X
				sby = r.y < 0 ? 0 : r.y; // 原图中实际可裁剪区域的起始Y
				sex = r.x + dw - 1; // 原图中裁剪区域的起终止X
				sey = r.y + dh - 1; // 原图中裁剪区域的起终止Y
				dbx = r.x < 0 ? - r.x : 0;
				dby = r.y < 0 ? - r.y : 0;
				cw = sex > sw - 1 ? sw - sbx : sex - sbx + 1; // 实际可裁剪的像素宽
				ch = sey > sh - 1 ? sh - sby : sey - sby + 1; // 实际可裁剪的像素高
//				if (image.getType().yhm == 2) {
//					cw &= ~1;
//				}
//				if (image.getType().yvm == 2) {
//					ch &= ~1;
//				}
				
				pixelBytes = image.getType().uvs;
				si = (sby * sw + sbx) * pixelBytes;
				slbn = sw * pixelBytes;
				di = (dby * dw + dbx) * pixelBytes;
				dlbn = dw * pixelBytes;
				clbn = cw * pixelBytes;
				for (int i = 0; i < ch; i++) {
					System.arraycopy(sd, srcOffset + si, dd, desOffset + di, clbn);
					si += slbn;
					di += dlbn;
				}
				if (image.getType().pn > 2) {
					// UV分量也是独立存储
					srcOffset += sw * sh;
					desOffset += dw * dh;
					si = (sby * sw + sbx) * pixelBytes;
					di = (dby * dw + dbx) * pixelBytes;
					for (int i = 0; i < ch; i++) {
						System.arraycopy(sd, srcOffset + si, dd, desOffset + di, clbn);
						si += slbn;
						di += dlbn;
					}
				}
			}
		}
		return img;
	}
	/**
	 * 将一个图像转换为灰度图(比toGrayYImage快大约10%)
	 * 如果指定的图像本身就是灰度图则返回传入图像本身
	 * @param image 待转换图像
	 * @return
	 */
	public static UniGrayImage toGrayImage(UniImage image) {
		if (image instanceof UniGrayImage) {
			return (UniGrayImage)image.create(true);
		}
		if (image instanceof UniRGBImage) {
			UniGrayImage img = new UniGrayImage(image.getWidth(), image.getHeight());
			img.setInsideGeometryGraph(UniGeometryGraph.clone(image.getInsideGeometryGraph()));
			img.setDegree(image.getDegree());
			img.setOrientation(image.getOrientation());
			img.setMirror(image.isMirror());
			int srcPixelByteNum = image.pixelBits() / 8;
			int desPixelByteNum = img.pixelBits() / 8;
			for (int si = 0, di = 0; si < image.getImageData().length; si += srcPixelByteNum, di += desPixelByteNum) {
				int sum = 0;
				for (int i = 0; i < srcPixelByteNum; i++) {
					sum += image.getImageData()[si + i] & 0xFF;
				}
				img.getImageData()[di] = (byte) (sum / srcPixelByteNum);
			}
			return img;
		}
		if (image instanceof UniYUVImage) {
			UniGrayImage img = new UniGrayImage(image.getWidth(), image.getHeight());
			img.setInsideGeometryGraph(UniGeometryGraph.clone(image.getInsideGeometryGraph()));
			img.setDegree(image.getDegree());
			img.setOrientation(image.getOrientation());
			img.setMirror(image.isMirror());
			System.arraycopy(image.getImageData(), 0, img.getImageData(), 0, img.getImageData().length);
			return img;
		}
		return null;
	}
	/**
	 * 将一个图像转换为灰度图(更适合人脸识别的算法版本)
	 * 如果指定的图像本身就是灰度图则返回传入图像本身
	 * @param image 待转换图像
	 * @return
	 */
	public static UniGrayYImage toGrayYImage(UniImage image) {
		if (image instanceof UniGrayYImage) {
			return (UniGrayYImage)image.create(true);
		}
		if (image instanceof UniRGBImage) {
			RGBType type = ((UniRGBImage)image).getType();
			UniGrayYImage img = new UniGrayYImage(image.getWidth(), image.getHeight());
			img.setInsideGeometryGraph(UniGeometryGraph.clone(image.getInsideGeometryGraph()));
			img.setDegree(image.getDegree());
			img.setOrientation(image.getOrientation());
			img.setMirror(image.isMirror());
			int srcPixelByteNum = image.pixelBits() / 8;
			int desPixelByteNum = img.pixelBits() / 8;
			for (int si = 0, di = 0; si < image.getImageData().length; si += srcPixelByteNum, di += desPixelByteNum) {
                int R = image.getImageData()[si + type.ri] & 0xFF;
                int G = image.getImageData()[si + type.gi] & 0xFF;
                int B = image.getImageData()[si + type.bi] & 0xFF;
                int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
                img.getImageData()[di] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
			}
			return img;
		}
		if (image instanceof UniYUVImage) {
			UniGrayYImage img = new UniGrayYImage(image.getWidth(), image.getHeight());
			img.setInsideGeometryGraph(UniGeometryGraph.clone(image.getInsideGeometryGraph()));
			img.setDegree(image.getDegree());
			img.setOrientation(image.getOrientation());
			img.setMirror(image.isMirror());
			System.arraycopy(image.getImageData(), 0, img.getImageData(), 0, img.getImageData().length);
			return img;
		}
		return null;
	}
	public static UniRGBImage toRGBImage(UniImage image) {
		if (image instanceof UniYUVImage) {
			return yuv2rgb((UniYUVImage)image);
		}
		if (((UniRGBImage)image).getType().bits == 8) {
			return gray2rgb((UniRGBImage)image);
		}
		return (UniRGBImage)image;
	}
	public static UniRGBImage gray2rgb(UniRGBImage image) {
		UniRGBImage img = new UniRGBImage(image.getWidth(), image.getHeight());
		int di = 0;
		for (int i = 0; i < image.getImageData().length; i++) {
			img.getImageData()[di++] = image.getImageData()[i];
			img.getImageData()[di++] = image.getImageData()[i];
			img.getImageData()[di++] = image.getImageData()[i];
		}
		img.setInsideGeometryGraph(UniGeometryGraph.clone(image.getInsideGeometryGraph()));
		img.setDegree(image.getDegree());
		img.setOrientation(image.getOrientation());
		img.setMirror(image.isMirror());
		return img;
	}
	public static UniRGBImage yuv2rgb(UniYUVImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		UniRGBImage img = new UniRGBImage(w, h);
		img.setInsideGeometryGraph(UniGeometryGraph.clone(image.getInsideGeometryGraph()));
		img.setDegree(image.getDegree());
		img.setOrientation(image.getOrientation());
		img.setMirror(image.isMirror());
		RGBType rgb = img.getType();
		YUVType yuv = image.getType();
		int yhm = yuv.yhm;
		int yvm = yuv.yvm;
		int pn = yuv.pn;
		int yi = yuv.ysi;
		int ui = yuv.usi;
		int vi = yuv.vsi;
		int ps = 0; // 填充区长度
		if (pn > 1) {
			// y分量单独存储格式
			int wh = w * h;
			if (ui == vi) {
				// uv分量都是单独存储的格式
				int uvdl = (wh * 2) / (yhm * yvm); // uv分量数据长度
				if (wh + uvdl < image.getImageData().length) {
					// uv分量存储区带填充
					ps = w / 2;
				} else {
					// uv分量存储区无填充
					ps = 0;
				}
				if (yuv.uv) {
					ui += wh;
					vi += wh + (image.getImageData().length - wh) / 2;
				} else {
					vi += wh;
					ui += wh + (image.getImageData().length - wh) / 2;
				}
			} else {
				// uv分量混合存储格式
				ui += wh;
				vi += wh;
			}
		} else {
			// yuv分量混合存储格式
		}
		
		int desByteNum = img.pixelBits() / 8;
		int di = 0;
		byte[] sd = image.getImageData();
		byte[] dd = img.getImageData();
		int yc = 0;
		int uvw = w / (yuv.pn - 1);
		for (int y = 0; y < h; y++) {
			int xc = 0;
			for (int x = 0; x < w; x++) {
				int Y = sd[yi] & 0xFF;
				int U = sd[ui] & 0xFF;
				int V = sd[vi] & 0xFF;
				int R = ((yuv.ry * Y + yuv.ru * U + yuv.rv * V + yuv.rc) >> 8);
				int G = ((yuv.gy * Y + yuv.gu * U + yuv.gv * V + yuv.gc) >> 8);
				int B = ((yuv.by * Y + yuv.bu * U + yuv.bv * V + yuv.bc) >> 8);
				R = R < 0 ? 0 : (R > 255 ? 255 : R);
				G = G < 0 ? 0 : (G > 255 ? 255 : G);
				B = B < 0 ? 0 : (B > 255 ? 255 : B);
				dd[di + rgb.ri] = (byte)B;
				dd[di + rgb.gi] = (byte)G;
				dd[di + rgb.bi] = (byte)R;
				
//				dd[di + rgb.ri] = sd[yi];
//				dd[di + rgb.gi] = sd[yi];
//				dd[di + rgb.bi] = sd[yi];
				
				di += desByteNum;
				yi += yuv.ys;
				if (++xc >= yuv.yhm) {
					xc = 0;
					ui += yuv.uvs;
					vi += yuv.uvs;
				}
			}
			ui += ps;
			vi += ps;
			if (++yc < yuv.yvm) {
				ui -= uvw;
				vi -= uvw;
			} else {
				yc = 0;
			}
		}
		return img;
	}
	/**
	 * 旋转点位
	 * @param origin 旋转原点位置
	 * @param points 待进行旋转计算的点位
	 * @param degree 旋转角度
	 */
	public static void rotatePoints(UniPoint origin, UniPoint[] points, int degree) {
		degree %= 360;
		if (degree < 0)
			degree = 360 + degree;// 将角度转换到0-360度之间
		if (degree == 0) {
			return;
		}
		for (UniPoint point : points) {
			UniGeometryGraph.rotatePoint(origin.x, origin.y, point, degree, 0, 0);
		}
	}
	/**
	 * 在图像数据中用一段数据采用缩放算法绘制一根有方向线段
	 * @param imageData 图像数据
	 * @param idOffset 图像数据的起始位置
	 * @param imageSize 图像像素尺寸
	 * @param pixelByteNum 每像素的字节数
	 * @param beginPoint 线段起始点位
	 * @param endPoint 线段结束点位
	 * @param lineData 用来绘制线段的数据
	 * @param ldOffset 有效数据在lineData中的起始位置
	 * @param ldLength 有效数据的字节长度
	 * @param drawnPoints 用来返回线段绘制过程中每个像素的绘制点位
	 * @return 绘制的像素计数（包括因超出图像区域未执行实际绘制操作的像素）
	 */
	public static int directDrawLine(
			byte[] imageData, int idOffset, UniSize imageSize, int pixelByteNum, 
			UniPoint beginPoint, UniPoint endPoint, 
			byte[] lineData, int ldOffset, int ldLength, 
			List<UniPoint> drawnPoints) {
		int count = 0;
		int ldsp = ldOffset;
		int ldlen = ldLength;
		byte[] ld;
		if (lineData == null) {
			ld = new byte[pixelByteNum];
			ldsp = 0;
			ldlen = ld.length;
		} else {
			ld = lineData;
		}
		int lpn = ldlen / pixelByteNum; // 线数据定义的像素点数量
		// 如果是有效绘图或计算绘图点位的调用，则递归调用一次用来计算实际需要绘制的点数。
		int dpn = (imageData == null && drawnPoints == null) ? lpn : directDrawLine(null, idOffset, imageSize, pixelByteNum, beginPoint, endPoint, ld, ldsp, ldlen, null);
		int w = imageSize.width;
		int h = imageSize.height;
		int sx = beginPoint.x;
		int sy = beginPoint.y;
		int ex = endPoint.x;
		int ey = endPoint.y;
		int xp = sx; // 绘制循环中当前绘制像素点的x坐标
		int yp = sy; // 绘制循环中当前绘制像素点的y坐标
		int xs = sx > ex ? -1 : 1; // xp的像素循环增量
		int ys = sy > ey ? -1 : 1; // yp的像素循环增量
		int aw = ex > sx ? ex - sx  + 1 : sx - ex + 1; // 由待绘制线段两个端点构成的矩形的宽度
		int ah = ey > sy ? ey - sy + 1 : sy - ey + 1; // 由待绘制段两个端点构成的矩形的高度
		int di = (yp * w + xp) * pixelByteNum; // 由xp和yp确定的绘制像素点在imageData中的位置索引
		int pxs = xs * pixelByteNum; // p在x轴的字节循环增量
		int pys = ys * pixelByteNum * w; // p在y轴的字节循环增量
		int ldp = ldsp; // 线数据索引位置
		int var = 0; // 比差计算值
		int lvar = 0; // lineData分配的比差计算值
		boolean bold = drawnPoints == null;
		
		// 循环逐像素绘制线段
		// 为了避免在循环中执行乘除法运算，采用了比差算法
		if (ah > aw) {
			while (true) {
				if (xp >= 0 && xp < w && yp >= 0 && yp < h) {
					if (imageData != null) {
						// 绘制像素点
						for (int i = 0; i < pixelByteNum; i++) {
							imageData[idOffset + di + i] = ld[ldp + i];
						}
					}
				}
				if (drawnPoints != null) {
					drawnPoints.add(new UniPoint(xp, yp));
				}
				count++;
				lvar += lpn;
				while (lvar >= dpn) {
					lvar -= dpn;
					// 绘制源数据位置比差增量
					ldp += pixelByteNum;
				}
//				if (lpn > dpn) {
//					lvar += dpn;
//					while (lvar >= lpn) {
//						lvar -= lpn;
//						// 绘制源数据位置比差增量
//						ldp += pixelByteNum;
//					}
//				} else {
//					lvar += lpn;
//					while (lvar >= dpn) {
//						lvar -= dpn;
//						// 绘制源数据位置比差增量
//						ldp += pixelByteNum;
//					}
//				}
				// 行像素位置和数据位置增量
				yp += ys;
				di += pys;
				// 比差计算
				var += aw;
				while (var >= ah) {
					var -= ah;
					if (bold && xp >= 0 && xp < w && yp >= 0 && yp < h) {
						if (imageData != null) {
							// 绘制像素点
							for (int i = 0; i < pixelByteNum; i++) {
								imageData[idOffset + di + i] = ld[ldp + i];
							}
						}
					}
					if (drawnPoints != null) {
						drawnPoints.add(new UniPoint(xp, yp));
					}
					count++;
					lvar += lpn;
					while (lvar >= dpn) {
						lvar -= dpn;
						// 绘制源数据位置比差增量
						ldp += pixelByteNum;
					}
//					if (lpn > dpn) {
//						lvar += dpn;
//						while (lvar >= lpn) {
//							lvar -= lpn;
//							// 绘制源数据位置比差增量
//							ldp += pixelByteNum;
//						}
//					} else {
//						lvar += lpn;
//						while (lvar >= dpn) {
//							lvar -= dpn;
//							// 绘制源数据位置比差增量
//							ldp += pixelByteNum;
//						}
//					}
					// 列像素位置和数据位置比差增量
					xp += xs;
					di += pxs;
				}
				int ad = xs > 0 ? ex - xp : xp - ex;
				if (ad < 0) {
					// 当前目标绘制点位的x坐标值已经超出endPoint指定范围
					break;
				}
				ad = ys > 0 ? ey - yp : yp - ey;
				if (ad < 0) {
					// 当前目标绘制点位的y坐标值已经超出endPoint指定范围
					break;
				}
			}
		} else {
			while (true) {
				if (xp >= 0 && xp < w && yp >= 0 && yp < h) {
					if (imageData != null) {
						// 绘制像素点
						for (int i = 0; i < pixelByteNum; i++) {
							imageData[idOffset + di + i] = ld[ldp + i];
						}
					}
				}
				if (drawnPoints != null) {
					drawnPoints.add(new UniPoint(xp, yp));
				}
				count++;
				lvar += lpn;
				while (lvar >= dpn) {
					lvar -= dpn;
					// 绘制源数据位置比差增量
					ldp += pixelByteNum;
				}
//				if (lpn > dpn) {
//					lvar += dpn;
//					while (lvar >= lpn) {
//						lvar -= lpn;
//						// 绘制源数据位置比差增量
//						ldp += pixelByteNum;
//					}
//				} else {
//					lvar += lpn;
//					while (lvar >= dpn) {
//						lvar -= dpn;
//						// 绘制源数据位置比差增量
//						ldp += pixelByteNum;
//					}
//				}
				// 列像素位置和数据位置增量
				xp += xs;
				di += pxs;
				// 比差计算
				var += ah;
				while (var >= aw) {
					var -= aw;
					if (bold && xp >= 0 && xp < w && yp >= 0 && yp < h) {
						if (imageData != null) {
							// 绘制像素点
							for (int i = 0; i < pixelByteNum; i++) {
								imageData[idOffset + di + i] = ld[ldp + i];
							}
						}
					}
					if (drawnPoints != null) {
						drawnPoints.add(new UniPoint(xp, yp));
					}
					count++;
					lvar += lpn;
					while (lvar >= dpn) {
						lvar -= dpn;
						// 绘制源数据位置比差增量
						ldp += pixelByteNum;
					}
//					if (lpn > dpn) {
//						lvar += dpn;
//						while (lvar >= lpn) {
//							lvar -= lpn;
//							// 绘制源数据位置比差增量
//							ldp += pixelByteNum;
//						}
//					} else {
//						lvar += lpn;
//						while (lvar >= dpn) {
//							lvar -= dpn;
//							// 绘制源数据位置比差增量
//							ldp += pixelByteNum;
//						}
//					}
					// 行像素位置和数据位置比差增量
					yp += ys;
					di += pys;
				}
				int ad = xs > 0 ? ex - xp : xp - ex;
				if (ad < 0) {
					// 当前目标绘制点位的x坐标值已经超出endPoint指定范围
					break;
				}
				ad = ys > 0 ? ey - yp : yp - ey;
				if (ad < 0) {
					// 当前目标绘制点位的y坐标值已经超出endPoint指定范围
					break;
				}
			}
		}
		return count;
	}
	/**
	 * 旋转RGB图片
	 * @param image 待旋转图片
	 * @param degree 旋转角度
	 * @param bgcolor 旋转产生的空区填充色，只支持灰度色
	 * @param desSize 旋转图片的尺寸。null表示与原图一致；(0, 0)表示自适应，旋转图尺寸恰好完整显示旋转以后的图片内容；指定了其他值则表示定制尺寸，旋转后的内容居中。
	 * @return 旋转后的图片
	 */
	public static UniRGBImage rotateImage(UniRGBImage image, int degree, byte bgcolor, UniSize desSize) {
		degree %= 360;
		if (degree < 0)
			degree = 360 + degree;// 将角度转换到0-360度之间
		if (desSize == null || desSize.width == 0 && desSize.height == 0) {
			if (degree == 0) {
				return (UniRGBImage) image.create(true);
			}
			if (degree == 180) {
				int wh = image.getWidth() * image.getHeight();
				UniRGBImage img = (UniRGBImage) image.create(false);
				byte[] sd = image.getImageData();
				byte[] dd = img.getImageData();
				int offset = 0;
				int pbn =image.pixelBits() / 8;
				int si = 0;
				int di = (wh - 1) * pbn;
				while (di > 0) {
					for (int i = 0; i < pbn; i++) {
						dd[offset + di + i] = sd[offset + si + i];
					}
					si += pbn;
					di -= pbn;
				}
				Integer io = image.getOrientation();
				if (io != null) {
					img.setOrientation(io - degree);
				}
				img.setDegree(image.getDegree());
				img.setMirror(image.isMirror());
				img.setInsideGeometryGraph(UniGeometryGraph.rotate(UniGeometryGraph.clone(image.getInsideGeometryGraph()), image.getWidth() >> 1, image.getHeight() >> 1, degree, 0, 0));
				return img;
			} else if (degree == 90 || degree == 270) {
				UniRGBImage img = (UniRGBImage) image.create(false);
				int w = image.getWidth();
				int h = image.getHeight();
				if (desSize != null) {
					img.setWidth(h);
					img.setHeight(w);
				} else {
					// 用背景灰度色值填充
					Arrays.fill(img.getImageData(), bgcolor);
				}
				int dw = img.getWidth();
				int dh = img.getHeight();
				
				int pbn =image.pixelBits() / 8;
				int srbn = w * pbn; // 原图一行像素的字节数
				int drbn = dw * pbn; // 目标图一行像素的字节数
				int sbx = 0; // 原图扫描的起始X
				int sby = 0; // 原图扫描的起始Y
				int sex = w - 1; // 原图扫描的终止X
				int sey = h - 1; // 原图扫描的终止Y
				int dbx = 0; // 目标图扫描的起始X
				int dby = 0; // 目标图扫描的起始Y
				if (desSize == null) {
					if (w > h) {
						sbx = (w - h) / 2;
						sby = 0;
						sex = w - (w - h) / 2 - 1;
						sey = h - 1;
					} else {
						sbx = 0;
						sby = (h - w) / 2;
						sex = w - 1;
						sey = h - (h - w) / 2 - 1;
					}
					if (degree == 90) {
						dbx = sex;
						dby = sby;
					} else {
						dbx = sbx;
						dby = sey;
					}
				} else {
					if (degree == 90) {
						dbx = dw - 1;
						dby = 0;
					} else {
						dbx = 0;
						dby = dh - 1;
					}
				}
				
				int si = sby * srbn + sbx * pbn;
				int nsi = si;
				int di = dby * drbn + dbx * pbn;;
				int ndi = di;
				
				byte[] sd = image.getImageData();
				byte[] dd = img.getImageData();
				for (int y = sby; y <= sey; y++) {
					for (int x = sbx; x <= sex; x++) {
						for (int i = 0; i < pbn; i++) {
							dd[di + i] = sd[si + i];
						}
						si += pbn;
						if (degree == 90) {
							di += drbn;
						} else {
							di -= drbn;
						}
					}
					nsi += srbn;
					si = nsi;
					if (degree == 90) {
						ndi -= pbn;
					} else {
						ndi += pbn;
					}
					di = ndi;
				}
				Integer io = image.getOrientation();
				if (io != null) {
					img.setOrientation(io - degree);
				}
				img.setDegree(image.getDegree());
				img.setMirror(image.isMirror());
				img.setInsideGeometryGraph(UniGeometryGraph.rotate(UniGeometryGraph.clone(image.getInsideGeometryGraph()), image.getWidth() >> 1, image.getHeight() >> 1, degree, (img.getWidth() - image.getWidth()) >> 1, (img.getHeight() - image.getHeight()) >> 1));
				return img;
			}
		}
		// 源图原点
		UniPoint imageOrigin = new UniPoint(image.getWidth() / 2, image.getHeight() / 2);
		UniRectPoints rectPoints = null;
		int w = image.getWidth();
		int h = image.getHeight();
		if (desSize != null) {
			if (desSize.width == 0 && desSize.height == 0) {
				rectPoints = new UniRectPoints(0, 0, image.getWidth(), image.getHeight());
				// 计算图片4个顶点在原图中旋转以后的坐标
				rotatePoints(imageOrigin, rectPoints.getPoints(), degree);
				// 计算旋转以后所占的矩形宽和高
				int left = Integer.MAX_VALUE;
				int right = Integer.MIN_VALUE;
				int top = Integer.MAX_VALUE;
				int bottom = Integer.MIN_VALUE;
				for (UniPoint p : rectPoints.getPoints()) {
					if (left > p.x) {
						left = p.x;
					}
					if (right < p.x) {
						right = p.x;
					}
					if (top > p.y) {
						top = p.y;
					}
					if (bottom < p.y) {
						bottom = p.y;
					}
				}
				w = right - left + 1 + 1;
				h = bottom - top + 1 + 1;
				if (w * h < image.getWidth() * image.getHeight()) {
					w++; h++;
				}
			} else {
				// 指定旋转图的宽高
				w = desSize.width;
				h = desSize.height;
			}
		}
		UniRGBImage rotate = (UniRGBImage) image.create(w, h);
		
		// 旋转图原点
		UniPoint rotateOrigin = new UniPoint(rotate.getWidth() / 2, rotate.getHeight() / 2);
		// 通过原图原点与旋转图原点使待旋转区域在旋转图中居中
		int x = rotateOrigin.x - imageOrigin.x;
		int y = rotateOrigin.y - imageOrigin.y;
		rectPoints = new UniRectPoints(x, y, image.getWidth(), image.getHeight());
		
		// 计算图片4个顶点旋转以后的坐标
		rotatePoints(rotateOrigin, rectPoints.getPoints(), degree);
		
		UniSize rotateSize = new UniSize(rotate.getWidth(), rotate.getHeight());
		// 计算图片左边线旋转以后线上的各个点坐标
		List<UniPoint> left = new LinkedList<UniPoint>();
		int pixelByteNum = image.pixelBits() / 8;
		int idOffset = 0;
		directDrawLine(null, idOffset, rotateSize, pixelByteNum, rectPoints.getLeftTop(), rectPoints.getLeftBottom(), null, 0, 0, left);
		// 计算图片右边线旋转以后线上的各个点坐标
		List<UniPoint> right = new LinkedList<UniPoint>();
		directDrawLine(null, idOffset, rotateSize, pixelByteNum, rectPoints.getRightTop(), rectPoints.getRightBottom(), null, 0, 0, right);
		
		// 用背景灰度色值填充
		Arrays.fill(rotate.getImageData(), bgcolor);
		
		// 行数据字节长度
		int lbn = image.getWidth() * pixelByteNum;
		// 计算逐行绘制旋转图片过程中比差算法所需的各项参数
		int rows = image.getHeight();
		int size = left.size() > right.size() ? right.size() : left.size();
		int si = 0; // 原图片行数据的索引位置
		int val = rows > size ? size : rows;
		// 根据left和right记录的左右线点位，逐行绘制原图
		for (int i = 0; i < size && si + lbn <= image.getImageData().length; i++) {
			// 用原图行数据绘制一行旋转后的图片行
			directDrawLine(rotate.getImageData(), idOffset, rotateSize, pixelByteNum, left.get(i), right.get(i), image.getImageData(), si, lbn, null);
			// 比差调整si
			if (rows > size) {
				val += size;
				while (val >= rows) {
					val -= rows;
					si += lbn;
				}
			} else {
				val += rows;
				while (val >= size) {
					val -= size;
					si += lbn;
				}
			}
		}
		Integer io = image.getOrientation();
		if (io != null) {
			rotate.setOrientation(io - degree);
		}
		rotate.setDegree(image.getDegree());
		rotate.setMirror(image.isMirror());
		int translateX = (rotate.getWidth() - image.getWidth()) >> 1;
		int translateY = (rotate.getHeight() - image.getHeight()) >> 1;
		rotate.setInsideGeometryGraph(UniGeometryGraph.rotate(UniGeometryGraph.clone(image.getInsideGeometryGraph()), image.getWidth() >> 1, image.getHeight() >> 1, degree, translateX, translateY));
		return rotate;
	}
	/**
	 * 旋转YUV图片
	 * 暂时只保持原图尺寸或自适应尺寸且整90度的旋转
	 * @param image 待旋转图片
	 * @param degree 旋转角度
	 * @param bgcolor 旋转产生的空区填充色，只支持灰度色
	 * @param desSize 旋转图片的尺寸。null表示与原图一致；(0, 0)表示自适应，旋转图尺寸恰好完整显示旋转以后的图片内容；指定了其他值则表示定制尺寸，旋转后的内容居中。
	 * @return 旋转后的图片
	 */
	public static UniYUVImage rotateImage(UniYUVImage image, int degree, byte bgcolor, UniSize desSize) {
		degree %= 360;
		if (degree < 0)
			degree = 360 + degree;// 将角度转换到0-360度之间
		if (desSize == null || desSize.width == 0 && desSize.height == 0) {
			if (degree == 0) {
				return (UniYUVImage) image.create(true);
			}
			if (degree == 180) {
				int wh = image.getWidth() * image.getHeight();
				UniYUVImage img = (UniYUVImage) image.create(false);
				byte[] sd = image.getImageData();
				byte[] dd = img.getImageData();
				int offset = 0;
				int pbn = image.getType().ys;
				int si = 0;
				int di = (wh - 1) * pbn;
				int end = di + 1;
				while (di >= 0) {
					for (int i = 0; i < pbn; i++) {
						dd[offset + di + i] = sd[offset + si + i];
					}
					si += pbn;
					di -= pbn;
				}
				if (image.getType().pn > 1) {
					// Y分量是独立存储的
					int yhvm = image.getType().yhm * image.getType().yvm;
					
					offset = end;
					si = 0;
					pbn = image.getType().uvs;
					di = (wh / yhvm - 1) * pbn;
					end = di + 1;
					while (di >= 0) {
						for (int i = 0; i < pbn; i++) {
							dd[offset + di + i] = sd[offset + si + i];
						}
						si += pbn;
						di -= pbn;
					}
					if (image.getType().pn > 2) {
						// UV分量也是独立存储的
						offset = end;
						si = 0;
						di = (wh / yhvm - 1) * pbn;
						while (di >= 0) {
							for (int i = 0; i < pbn; i++) {
								dd[offset + di + i] = sd[offset + si + i];
							}
							si += pbn;
							di -= pbn;
						}
					}
				}
				Integer io = image.getOrientation();
				if (io != null) {
					img.setOrientation(io - degree);
				}
				img.setDegree(image.getDegree());
				img.setMirror(image.isMirror());
				img.setInsideGeometryGraph(UniGeometryGraph.rotate(UniGeometryGraph.clone(image.getInsideGeometryGraph()), image.getWidth() >> 1, image.getHeight() >> 1, degree, 0, 0));
				return img;
			} else if (degree == 90 || degree == 270) {
				UniYUVImage img = (UniYUVImage) image.create(false);
				int w = image.getWidth();
				int h = image.getHeight();
				if (desSize != null) {
					img.setWidth(h);
					img.setHeight(w);
				} else {
					// 临时实现仅填充Y分量
					Arrays.fill(img.getImageData(), 0, w * h, bgcolor);
				}
				int dw = img.getWidth();
				int dh = img.getHeight();
				
				int offset = 0;
				int pbn = image.getType().ys;
				int srbn = w * pbn; // 原图一行像素的字节数
				int drbn = dw * pbn; // 目标图一行像素的字节数
				int sbx = 0; // 原图扫描的起始X
				int sby = 0; // 原图扫描的起始Y
				int sex = w - 1; // 原图扫描的终止X
				int sey = h - 1; // 原图扫描的终止Y
				int dbx = 0; // 目标图扫描的起始X
				int dby = 0; // 目标图扫描的起始Y
				if (desSize == null) {
					if (w > h) {
						sbx = (w - h) / 2;
						sby = 0;
						sex = w - (w - h) / 2 - 1;
						sey = h - 1;
					} else {
						sbx = 0;
						sby = (h - w) / 2;
						sex = w - 1;
						sey = h - (h - w) / 2 - 1;
					}
					if (degree == 90) {
						dbx = sex;
						dby = sby;
					} else {
						dbx = sbx;
						dby = sey;
					}
				} else {
					if (degree == 90) {
						dbx = dw - 1;
						dby = 0;
					} else {
						dbx = 0;
						dby = dh - 1;
					}
				}
				
				int si = sby * srbn + sbx * pbn;
				int nsi = si;
				int di = dby * drbn + dbx * pbn;;
				int ndi = di;
				
				byte[] sd = image.getImageData();
				byte[] dd = img.getImageData();
				for (int y = sby; y <= sey; y++) {
					for (int x = sbx; x <= sex; x++) {
						for (int i = 0; i < pbn; i++) {
							dd[offset + di + i] = sd[offset + si + i];
						}
						si += pbn;
						if (degree == 90) {
							di += drbn;
						} else {
							di -= drbn;
						}
					}
					nsi += srbn;
					si = nsi;
					if (degree == 90) {
						ndi -= pbn;
					} else {
						ndi += pbn;
					}
					di = ndi;
				}
				if (image.getType().pn > 1) {
					// Y分量是独立存储的
					offset = w * h;
					w /= image.getType().yhm;
					h /= image.getType().yvm;
					if (desSize != null) {
						dw = h;
						dh = w;
					} else {
						dw = w;
						dh = h;
					}
					pbn = image.getType().uvs;
					srbn = w * pbn; // 原图一行像素的字节数
					drbn = dw * pbn; // 目标图一行像素的字节数
					sbx = 0; // 原图扫描的起始X
					sby = 0; // 原图扫描的起始Y
					sex = w - 1; // 原图扫描的终止X
					sey = h - 1; // 原图扫描的终止Y
					dbx = 0; // 目标图扫描的起始X
					dby = 0; // 目标图扫描的起始Y
					if (desSize == null) {
						if (w > h) {
							sbx = (w - h) / 2;
							sby = 0;
							sex = w - (w - h) / 2 - 1;
							sey = h - 1;
						} else {
							sbx = 0;
							sby = (h - w) / 2;
							sex = w - 1;
							sey = h - (h - w) / 2 - 1;
						}
						if (degree == 90) {
							dbx = sex;
							dby = sby;
						} else {
							dbx = sbx;
							dby = sey;
						}
					} else {
						if (degree == 90) {
							dbx = dw - 1;
							dby = 0;
						} else {
							dbx = 0;
							dby = dh - 1;
						}
					}
					si = sby * srbn + sbx * pbn;
					nsi = si;
					di = dby * drbn + dbx * pbn;;
					ndi = di;
					for (int y = sby; y <= sey; y++) {
						for (int x = sbx; x <= sex; x++) {
							for (int i = 0; i < pbn; i++) {
								dd[offset + di + i] = sd[offset + si + i];
							}
							si += pbn;
							if (degree == 90) {
								di += drbn;
							} else {
								di -= drbn;
							}
						}
						nsi += srbn;
						si = nsi;
						if (degree == 90) {
							ndi -= pbn;
						} else {
							ndi += pbn;
						}
						di = ndi;
					}
					if (image.getType().pn > 2) {
						// UV分量也是独立存储的，暂时还没实现；
					}
				}
				Integer io = image.getOrientation();
				if (io != null) {
					img.setOrientation(io - degree);
				}
				img.setDegree(image.getDegree());
				img.setMirror(image.isMirror());
				img.setInsideGeometryGraph(UniGeometryGraph.rotate(UniGeometryGraph.clone(image.getInsideGeometryGraph()), image.getWidth() >> 1, image.getHeight() >> 1, degree, (img.getWidth() - image.getWidth()) >> 1, (img.getHeight() - image.getHeight()) >> 1));
				return img;
			}
		}
		// 以下暂时还不能用
		// 源图原点
		UniPoint imageOrigin = new UniPoint(image.getWidth() / 2, image.getHeight() / 2);
		UniRectPoints rectPoints = null;
		int w = image.getWidth();
		int h = image.getHeight();
		if (desSize != null) {
			if (desSize.width == 0 && desSize.height == 0) {
				rectPoints = new UniRectPoints(0, 0, image.getWidth(), image.getHeight());
				// 计算图片4个顶点在原图中旋转以后的坐标
				rotatePoints(imageOrigin, rectPoints.getPoints(), degree);
				// 计算旋转以后所占的矩形宽和高
				int left = Integer.MAX_VALUE;
				int right = Integer.MIN_VALUE;
				int top = Integer.MAX_VALUE;
				int bottom = Integer.MIN_VALUE;
				for (UniPoint p : rectPoints.getPoints()) {
					if (left > p.x) {
						left = p.x;
					}
					if (right < p.x) {
						right = p.x;
					}
					if (top > p.y) {
						top = p.y;
					}
					if (bottom < p.y) {
						bottom = p.y;
					}
				}
				w = right - left + 1 + 1;
				h = bottom - top + 1 + 1;
				if (w * h < image.getWidth() * image.getHeight()) {
					w++; h++;
				}
			} else {
				// 指定旋转图的宽高
				w = desSize.width;
				h = desSize.height;
			}
		}
		UniYUVImage rotate = (UniYUVImage) image.create(w, h);
		
		// 旋转图原点
		UniPoint rotateOrigin = new UniPoint(rotate.getWidth() / 2, rotate.getHeight() / 2);
		// 通过原图原点与旋转图原点使待旋转区域在旋转图中居中
		int x = rotateOrigin.x - imageOrigin.x;
		int y = rotateOrigin.y - imageOrigin.y;
		rectPoints = new UniRectPoints(x, y, image.getWidth(), image.getHeight());
		
		// 计算图片4个顶点旋转以后的坐标
		rotatePoints(rotateOrigin, rectPoints.getPoints(), degree);
		
		UniSize rotateSize = new UniSize(rotate.getWidth(), rotate.getHeight());
		// 计算图片左边线旋转以后线上的各个点坐标
		List<UniPoint> left = new LinkedList<UniPoint>();
		int pixelByteNum = image.pixelBits() / 8;
		int idOffset = 0;
		directDrawLine(null, idOffset, rotateSize, pixelByteNum, rectPoints.getLeftTop(), rectPoints.getLeftBottom(), null, 0, 0, left);
		// 计算图片右边线旋转以后线上的各个点坐标
		List<UniPoint> right = new LinkedList<UniPoint>();
		directDrawLine(null, idOffset, rotateSize, pixelByteNum, rectPoints.getRightTop(), rectPoints.getRightBottom(), null, 0, 0, right);
		
		// 用背景灰度色值填充
		Arrays.fill(rotate.getImageData(), bgcolor);
		
		// 行数据字节长度
		int lbn = image.getWidth() * pixelByteNum;
		// 计算逐行绘制旋转图片过程中比差算法所需的各项参数
		int rows = image.getHeight();
		int size = left.size() > right.size() ? right.size() : left.size();
		int si = 0; // 原图片行数据的索引位置
		int val = rows > size ? size : rows;
		// 根据left和right记录的左右线点位，逐行绘制原图
		for (int i = 0; i < size && si + lbn <= image.getImageData().length; i++) {
			// 用原图行数据绘制一行旋转后的图片行
			directDrawLine(rotate.getImageData(), idOffset, rotateSize, pixelByteNum, left.get(i), right.get(i), image.getImageData(), si, lbn, null);
			// 比差调整si
			if (rows > size) {
				val += size;
				while (val >= rows) {
					val -= rows;
					si += lbn;
				}
			} else {
				val += rows;
				while (val >= size) {
					val -= size;
					si += lbn;
				}
			}
		}
		return rotate;
	}
}
