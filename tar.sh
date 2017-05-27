rm -rf tar
mkdir tar
mkdir -p tar/data/corpus/wordlist
mkdir -p tar/data/model
mkdir -p tar/target
cp -r library tar/
cp -r lib tar/
cp readme.txt tar/
cp data/corpus/wordlist/* tar/data/corpus/wordlist
cp data/corpus/renminribao.txt.posPattern tar/data/corpus
cp data/model/* tar/data/model
cp target/newWordDetection-1.0-jar-with-dependencies.jar tar/target/detect.jar
cp data/corpus/wordlist/renminribao.txt.wordlist tar/data/corpus/wordlist/renminribao.txt.wordlist
cp config.properties tar/config.properties
