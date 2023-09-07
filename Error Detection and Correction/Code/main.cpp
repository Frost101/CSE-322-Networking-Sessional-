#include<bits/stdc++.h>
#include "errorCorrection.h"



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

//* Function prototypes
void adjustDataString(string &data, int m);
string convertToBinary(int ascii);
void createDataBlock(string data, int m);
void printDataBlock();
void printDataBlockWithCheckBits();
string serialize(int m);

//* Global variables
vector<string> dataBlock;

int main()
{
    string dataMain;        //^ data string, which is the string to be transmitted
    int m;                  //^ the number of bytes in a row of the data block
    double p;               //^ The probability of each bit being toggled during the transmission
    string genPolynomial;   //^ generator polynomial, it is used for calculating and verifying CRC checksum

    //* input
    cout << "enter data string: ";
    getline(cin, dataMain);

    cout << "enter number of data bytes in a row <m>: ";
    cin >> m;

    cout << "enter probability <p>: ";
    cin >> p;

    cout << "enter generator polynomial: ";
    cin >> genPolynomial;

    cout << endl;

    //* Step:1
    //* If the size of the data string is not a multiple of m,
    //* append the padding character (~) to the data string acordingly.
    adjustDataString(dataMain,m);

    //* Print the updated data string
    cout << "data string after padding: ";
    cout << dataMain << endl;
    cout << endl;


    //* Step:2
    //* Create data block
    //* Each row of data block conatins m characters
    createDataBlock(dataMain,m);

    //* Print data block
    //* There should be l/m rows
    printDataBlock();


    //* Step:03
    //* Add check bits to correct at most one-bit error in each row of data block
    addCheckBits(dataBlock,m);

    //* Print updated data block
    printDataBlockWithCheckBits();

    //* Step:04
    //* Serialize the above data block in  column major manner
    string serializedData = serialize(m);    
    //* Print
    cout << "data bits after column-wise serialization:" << endl;
    cout << serializedData << endl;
    cout << endl;

}

void adjustDataString(string &data, int m){
    //* If the size of the data string is not a multiple of m,
    //* append the padding character (~) to the data string acordingly.
    while(data.length()%m != 0){
        data.push_back('~');
    }
}


string convertToBinary(int ascii){
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

void createDataBlock(string data, int m){
    //* Each row contains ASCII codes of m characters.
    //* The first row shows the first m characters, the second row shows the next m characters
    //* and so on

    int rows = data.size() / m;
    int r = 0;
    //* Initialize
    dataBlock.clear();
    for(int i=0; i<rows; i++){
        dataBlock.push_back("");
    }

    for(int i=0; i<data.size(); i++){
        string tmp = convertToBinary(data[i]);
        dataBlock[r] += convertToBinary(data[i]);
        if((i+1)%m == 0) r++;       //^ increase block row number
    }
}

void printDataBlock(){

    cout << "data block <ascii code of m characters per row>:" << endl;

    for(int i=0; i<dataBlock.size(); i++){
        cout << dataBlock[i] << endl;
    }
    cout << endl;
}


void printDataBlockWithCheckBits(){

    cout << "data block after adding check bits:" << endl;

    int idx = 1;
    for(int i=0; i<dataBlock.size(); i++){
        idx = 1;
        for(int j=0; j<dataBlock[i].size(); j++){
            if(idx == j+1){
                //* Display check bits in green color
                cout << GREEN << dataBlock[i][j] << RESET;
                idx *= 2;
            }
            else{
                //* Display data bits in white
                cout << dataBlock[i][j];
            }
        }
        cout << endl;
    }

    cout << endl;
}


string serialize(int m){
    string tmp = "";
    int rows = dataBlock.size();
    for(int i=0; i< dataBlock[0].size(); i++){
        for(int j=0; j<rows; j++){
            tmp.push_back(dataBlock[j][i]);
        }
    }
    return tmp;
}