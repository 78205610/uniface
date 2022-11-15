/**
 * 
 */
package univideo;

/**
 * @author rechard
 *
 */
public class UniVideoInfo {

	private String url;
	private String format;
	private long duration;
	private long startTime;
	private int frames;
	private int width;
	private int height;
	private long bitRate;
	private double frameRate;
	private String decoderName;
	private int keyFrameInterval;
	/**
	 * 
	 */
	public UniVideoInfo() {
	}
	public String getUrl() {
		return url;
	}
	public UniVideoInfo setUrl(String url) {
		this.url = url;
		return this;
	}
	public String getFormat() {
		return format;
	}
	public UniVideoInfo setFormat(String format) {
		this.format = format;
		return this;
	}
	public long getDuration() {
		return duration;
	}
	public UniVideoInfo setDuration(long duration) {
		this.duration = duration;
		return this;
	}
	public long getStartTime() {
		return startTime;
	}
	public UniVideoInfo setStartTime(long startTime) {
		this.startTime = startTime;
		return this;
	}
	public int getFrames() {
		return frames;
	}
	public UniVideoInfo setFrames(int frames) {
		this.frames = frames;
		return this;
	}
	public int getWidth() {
		return width;
	}
	public UniVideoInfo setWidth(int width) {
		this.width = width;
		return this;
	}
	public int getHeight() {
		return height;
	}
	public UniVideoInfo setHeight(int height) {
		this.height = height;
		return this;
	}
	public long getBitRate() {
		return bitRate;
	}
	public UniVideoInfo setBitRate(long bitRate) {
		this.bitRate = bitRate;
		return this;
	}
	public double getFrameRate() {
		return frameRate;
	}
	public UniVideoInfo setFrameRate(double frameRate) {
		this.frameRate = frameRate;
		return this;
	}
	public String getDecoderName() {
		return decoderName;
	}
	public UniVideoInfo setDecoderName(String decoderName) {
		this.decoderName = decoderName;
		return this;
	}
	public int getKeyFrameInterval() {
		return keyFrameInterval;
	}
	public UniVideoInfo setKeyFrameInterval(int keyFrameInterval) {
		this.keyFrameInterval = keyFrameInterval;
		return this;
	}
	
	public UniVideoInfo(UniVideoInfo videoInfo) {
		this.setBitRate(videoInfo.getBitRate())
			.setDecoderName(videoInfo.getDecoderName())
			.setDuration(videoInfo.getDuration())
			.setFormat(videoInfo.getFormat())
			.setFrameRate(videoInfo.getFrameRate())
			.setFrames(videoInfo.getFrames())
			.setHeight(videoInfo.getHeight())
			.setStartTime(videoInfo.getStartTime())
			.setUrl(videoInfo.getUrl())
			.setWidth(videoInfo.getWidth())
			.setKeyFrameInterval(videoInfo.getKeyFrameInterval());
	}
	@Override
	public UniVideoInfo clone() {
		return new UniVideoInfo(this);
	}
	public String toString() {
		String str = String.format("video: %s", this.url);
		str += String.format("\r\nduration: %02d:%02d:%02d.%d, start time: %02d:%02d:%02d.%d, frames number: %d, kfi: %d", 
				this.duration / (1000 * 60 * 60),
				(this.duration % (1000 * 60 * 60)) / (1000 * 60),
				(this.duration % (1000 * 60)) / 1000,
				this.duration % 1000,
				this.startTime / (1000 * 60 * 60),
				(this.startTime % (1000 * 60 * 60)) / (1000 * 60),
				(this.startTime % (1000 * 60)) / 1000,
				this.startTime % 1000,
				this.frames,
				this.keyFrameInterval);
		str += String.format("\r\nformat: %s, size: %dx%d, bit rate: %.2fkb/s, frame rate: %.2ffp", 
				this.format,
				this.width, this.height,
				(float)this.bitRate / 1000.0f,
				this.frameRate);
		str += String.format("\r\ndecoder name: %s", this.decoderName);
		return str;
	}
}
