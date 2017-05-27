本程序可以在linux和windows上运行。
java -cp target/detect.jar main.Main -i <输入文件>
接下来当前文件会生成per.txt, loc.txt, org.txt, new.txt四个文件
分别对应输入文件中人名，地名，机构名，新词。
其中新词指的是人民日报语料2000年前3个月中未出现的词。词表见data/corpus/wordlist/renminribao.txt.wordlist。第一行为出现的词，第二行为其出现的频率。
可修改此文件来减少或者增大基本词表。
输出文件中，第一行为对应的人名，地名，机构名，新词，第二行为他们所在的上下文，其他各行为调试信息

