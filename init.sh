git clone https://github.com/taku910/crfpp
cd crfpp
./configure --prefix=`pwd`
sed -i '/#include "winmain.h"/d' crf_test.cpp
sed -i '/#include "winmain.h"/d' crf_learn.cpp
make -j4 install
cd ..
mv crfpp/bin/* lib/crfpp
