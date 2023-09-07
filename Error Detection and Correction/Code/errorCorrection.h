#include<bits/stdc++.h>
using namespace std;

//* Function Prototypes
int countParityBits(int m);
void addCheckBits(vector<string> &dataBlock, int m);
string convertToBinary2(int ascii);
void calculateParity(string &tmpStr, int p);

int countParityBits(int m){
    //* Number of check bits needed per row
    int cnt = 1;
    int tmp = 8 * m + 1;

    int i=0;
    while(true){
        if(tmp < cnt) return i;
        cnt *= 2;
        i++;
        tmp = 8*m + i;
    }
}


void addCheckBits(vector<string> &dataBlock, int m){
    int p = countParityBits(m);         //^ Number of redundant check bits to be added
    int rows = dataBlock.size();
    int length = m*8 + p;

    for(int r=0; r<rows; r++){
        //* initialize string
        string tmpStr = "";
        for(int i=0; i<length; i++){
            tmpStr.push_back('0');
        }

        // * Just marking the parity bit positions
        int idx = 1;
        for(int i=1; i<=p; i++){
            tmpStr[idx - 1] = '*';
            idx *= 2;   //^ next check bit position
        }

        //* Lets insert the data
        idx=0;
        for(int i=0; i<tmpStr.size(); i++){
            if(tmpStr[i]!='*'){
                //* If it's a position for data bit then
                tmpStr[i] = dataBlock[r][idx];
                idx++;
            }
            else{
                //* Replace the starts with 0
                //* Initial value of all check bits
                tmpStr[i]='0';
            }
        }

        //* Calculate & Adjust the parity bits
        calculateParity(tmpStr,p);
        dataBlock[r] = tmpStr;
    }

}


string convertToBinary2(int ascii){
    string binaryString;
    while(ascii){
        char tmp = (ascii % 2) + '0';
        binaryString.push_back(tmp);
        ascii /= 2;
    }
    
    //* adjust the binary to make it 8 bits long
    while(binaryString.size() < 8){
        binaryString.push_back('0');
    }

    reverse(binaryString.begin(), binaryString.end());
    return binaryString;
}


void calculateParity(string &tmpStr, int p){
    int length = tmpStr.size();
    int cnt = 0;        //^ No of one found

    int idxP = 1;
    for(int i=1; i<=p; i++){
        cnt = 0;
        for(int j=0; j<tmpStr.size(); j++){
            string tmp = convertToBinary2(j+1);
            string tmp2 = convertToBinary2(idxP);
            reverse(tmp.begin(), tmp.end());
            reverse(tmp2.begin(), tmp2.end());
            if(tmpStr[j] == '1' && tmp[i-1] == '1' && tmp2[i-1] == '1')cnt++;
        }

        //* Even Parity
        if(cnt%2) tmpStr[idxP-1] = '1';     //^ Adjusting the parity to one
        idxP *= 2;
    }
}
