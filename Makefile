.PHONY: all clean

out := hobbes_bin

all:
	mkdir -p $(out)
	mvn package
	cp target/tigerc-1.0-SNAPSHOT-jar-with-dependencies.jar $(out)/tigerc.jar

clean:
	mvn clean
	rm -f -- $(out)/tigerc.jar