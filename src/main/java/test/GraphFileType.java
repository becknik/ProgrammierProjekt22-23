package test;

public enum GraphFileType {
	TOY("toy.fmi"),
	STUTTGART("stgtregbz.fmi"),
	BW("bw.fmi"),
	GERMANY("germany.fmi");

	final String fileName;

	GraphFileType (final String sting) {
		this.fileName = sting;
	}

}
