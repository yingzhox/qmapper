package ac.ict.debs.qmapper.server;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Result {
	private boolean result;
	private String content;
	public boolean isResult() {
		return result;
	}
	public void setResult(boolean result) {
		this.result = result;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
