.PHONY: all clean build

SOURCE := $(wildcard src/main/java/com/tangzhixiong/jwatch/*.java)

all: build

clean:
	rm -rf target
build: target/jwatch.jar
target/jwatch.jar: $(SOURCE)
	mvn package
