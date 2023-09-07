#include<bits/stdc++.h>


// ANSI escape codes for text color
#define RESET   "\033[0m"      // Reset to default color
#define RED     "\033[31m"     // Red text
#define GREEN   "\033[32m"     // Green text
#define YELLOW  "\033[33m"     // Yellow text
#define BLUE    "\033[34m"     // Blue text
#define MAGENTA "\033[35m"     // Magenta text
#define CYAN    "\033[36m"     // Cyan text
#define WHITE   "\033[37m"     // White text


using namespace std;

void CRCchecksum(string &serializedData, string genPolynomial);
string mod2div(string divisor, string dividend);
void adjustRemianderLength(string &remainder,int len);
void printAppChecksum(string oldData, string remainder);
bool detectError(string receivedData, string genPolynomial);



string mod2div(string divisor, string dividend){
    string remainder;
    int size = divisor.size();
    if(divisor.size() > dividend.size()) return dividend;       // ^ Important

    int idx = size;     //^ To traverse the dividend      
    for(int i=0;i<size; i++){
        remainder.push_back(dividend[i]);
    }

    while(1){
        if(remainder.size() != divisor.size()) break;

        //* First do the X-OR operation
        for(int i=0; i<size; i++){
            char tmp = ((remainder[i] - '0') ^ (divisor[i] - '0')) + '0';
            remainder[i] = tmp;
        }

        //* Discard the leading zeros from the remainder
        string tmpStr;
        bool flag = false;
        for(int i=0; i<remainder.size(); i++){
            if(flag == false && remainder[i] == '1'){
                //* Found the first one
                flag = true;
            }

            if(flag){
                tmpStr.push_back(remainder[i]);
            }
        }
        //* Now Update the remainder
        remainder = tmpStr;

        //* Now pickup next bits from the dividend
        while(remainder.size() < divisor.size() && idx < dividend.size()){
            remainder.push_back(dividend[idx]);
            idx++;
        }
    }

    //* Remainder could be zero
    //* Must handle this case
    if(remainder.size()==0){
        remainder.push_back('0');
    }
    return remainder;
}



void CRCchecksum(string &serializedData, string genPolynomial){
    int divisorLen = genPolynomial.size();
    string oldData = serializedData;

    //* First append divisorLen - 1 no of '0' to the and of serialized Data
    for(int i=1; i<=divisorLen-1; i++){
        serializedData.push_back('0');
    } 

    string remainder = mod2div(genPolynomial, serializedData);
    //* Adjust remainder length, so that it could be divisorLen - 1
    adjustRemianderLength(remainder, divisorLen - 1);

    //* Print
    printAppChecksum(oldData,remainder);

    //* Merge
    oldData += remainder;
    serializedData = oldData;

}


void printAppChecksum(string oldData, string remainder){
    cout << "data bits after apending CRC checksum <sent frame>:" << endl;
    cout << oldData ;
    cout << CYAN << remainder << RESET << endl;
    cout << endl;
}

void adjustRemianderLength(string &remainder,int len){
    reverse(remainder.begin(), remainder.end());
    while(remainder.size() < len){
        remainder.push_back('0');
    }
    reverse(remainder.begin(), remainder.end());
}

bool detectError(string receivedData, string genPolynomial){
    string remainder = mod2div(genPolynomial, receivedData);
    if(remainder == "0") return false;
    else return true;
}
