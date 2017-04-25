# 新词发现毕业设计

## 0. 论文
- google scholar
> unknown OR new word detection OR identification
- cnki 
> 新词发现 新词识别 新词检测

## 1. 问题
- unicode中非utf8字符，可能有bug
- 低频词和数据稀疏性
- 按照自己的语料训练，可能繁体字和半角符号不知道会怎样。
- crf_test需要自己准备
- 这语料没有标好啊

## 2. 计划
- [ ] 5.10论文初稿
- [x] 字母词和数字去掉
- [x] BEMS变成分词文件和新词
- [x] 原始文件变成分词bems文件（单字特征
- [ ] 4.25看论文
- [ ] 4.25实现字特征的crf
- [ ] 4.25测试分词准确率
- [ ] 4.25确定测试数据
- [ ] 4.25保留连字符
- [ ] 4.25提取词典的词频


## 3.想法
- [x] 将所有标点符号当做断句的东西，而不只是句号
- [ ] 讨论评价的方法
- [ ] 搞更多的数据

## 4.运行
### 4.1 IDEA
    
    右键iml文件导入，右键pom.xml导入。
    
### 4.2 teminal
```shell
git clone https://github.com/chiyang10000/newWordDetection
cd newWordDetection
scp wanchiyang@lab:~/newWordDetection/data/model/*.model data/model/
mvn package
java -server -cp target/*with-dependencies.jar
```

1. evaluate.Corpus
    > 生成数据
2. evaluate.Test
    > 运行测试