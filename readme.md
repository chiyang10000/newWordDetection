# 新词发现毕业设计

## 论文
- google scholar
    > unknown OR new word detection OR identification
- cnki 
    > 新词发现 新词识别 新词检测
    
## 可以优化的地方
- 把StringFreq里面的计算左右熵的算法改一下
- 不算pmi,算pmi花了很多时间，而且没有用到
- 现在把字母串的过滤功能去掉了，对连字符拼接的字母串的识别效果不是很好

## 基本用法（repo里面已经有模型文件了，不用重新训练）
```
git clone https://github.com/chiyang10000/newWordDetection
cd newWordDetection
mvn package
./tar.sh
cd tar
./init.sh # 安装crfpp
java -cp target/detect.jar main.Main -i <输入文件>
接下来当前文件会生成per.txt, loc.txt, org.txt, new.txt四个文件
分别对应输入文件中人名，地名，机构名，新词。
其中新词指的是人民日报语料2000年前3个月中未出现的词。词表见data/corpus/wordlist/renminribao.txt.wordlist。第一行为出现的词，第二行为其出现的频率。
可修改此文件来减少或者增大基本词表。
输出文件中，第一行为对应的人名，地名，机构名，新词，第二行为他们所在的上下文，其他各行为调试信息
```

## 1.运行
### 1.1 IDEA
    
    右键iml文件导入，右键pom.xml导入。
    
### 1.2 teminal
```shell
git clone https://github.com/chiyang10000/newWordDetection
cd newWordDetection
mvn package
./init.sh # 安装 crfpp
java -server -cp target/*with-dependencies.jar <main.class>
```
1. dataProcess.Corpus
    > 生成数据
2. crfModel.charBased
    > 训练命名实体识别模型
3. crfModel.wordBased
    > 训练未登录词识别模型
4. evaluate.Test
    > 运行测试
    
## 2. 文件组织
1. data/
    > 原始数据和缓存数据
    1. data/model/
        > 放的是训练出来的模型文件 
    2. data/raw/
        > 放原始数据文件
    3. data/crf-template
        > 放crfpp模板文件
    4. data/corpus/
        > 放缓存的词表信息
    5. data/jupyter
        > 从info/生成报表
    6. data/test
        > 运行dataProcess.Corpus之后的生成的测试文件
2. library/
    > ansj的字典文件,用来修正一些分词错误
3. tmp/
    > 运行时的一些临时文件
4. info/
    > 运行的一些结果统计
6. target/
    > maven编译生成的jar包
7. tar.sh
    > 打包运行时的必要文件到tar这个文件夹里面
8. config.properties
    > 配置运行时的参数
