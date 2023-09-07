#include<bits/stdc++.h>
#include "errorCorrection.h"
#include "errorDetection.h"



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
void toggle(string &serializedData, vector<bool> &markRedBits, double p);
void printReceivedFrame(string receivedFrame, vector<bool> markedRedBits);
void removeCRCandDeserialize(string &receivedData, vector<bool> markRedBits, string genPolynomial, vector<vector<bool>> &newMarkRedBits);

//* Global variables
vector<string> dataBlock;
vector<string> rcvDataBlock;

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


    //* Step:05
    //* CRC checksum computation
    CRCchecksum(serializedData, genPolynomial);


    //* Step:06
    //* Simulate the physical transmision by toggling each bit
    //* of the stream with a probability p
    vector<bool> markRedBits;
    for(int i=0; i<serializedData.size(); i++){
        markRedBits.push_back(false);
    }
    toggle(serializedData,markRedBits,p);
    string receivedFrame = serializedData;
    //* Print
    printReceivedFrame(receivedFrame, markRedBits);


    //* Step:07 
    //* Verify the correctness of the received frame using the geenrator polynomial
    bool err = detectError(receivedFrame, genPolynomial);
    //* Print
    cout << "result of CRC checksum matching: ";
    if(err){
        cout << "error detected" << endl;
    }
    else{
        cout << "no error detected" << endl;
    }
    cout << endl;



    //* Step:08
    //* Remove the CRC checksum bits from the data stream
    //* and de-serialize it
    int rows = dataBlock.size();
    vector<vector<bool>> newMarkRedBits(rows);
    for(int i=0; i<rows; i++){
        for(int j=0; j<dataBlock[0].size(); j++){
            newMarkRedBits[i].push_back(false);
        }
    }

    //* Initialize receiver's data block
    rcvDataBlock.clear();
    for(int i=0; i<dataBlock.size(); i++){
        rcvDataBlock.push_back("");
    }

    removeCRCandDeserialize(receivedFrame, markRedBits, genPolynomial, newMarkRedBits);

}



void removeCRCandDeserialize(string &receivedData, vector<bool> markRedBits, string genPolynomial, vector<vector<bool>> &newMarkRedBits){
    //* Remove CRC check bits first
    int len = genPolynomial.size() - 1;
    while(len > 0){
        receivedData.pop_back();
        len--;
    }

    //* Deserialization begins
    int rowSize = dataBlock[0].size();
    int rows = dataBlock.size();
    int idx = 0;
    for(int i=0; i<rowSize; i++){
        for(int j=0; j<rows; j++){
            rcvDataBlock[j].push_back(receivedData[idx]);
            newMarkRedBits[j][i] = markRedBits[idx];
            idx++;
        }
    }

    //* print
    cout << "data block after removing CRC checksum bits:" << endl;
    for(int i=0; i<rcvDataBlock.size(); i++){
        for(int j=0; j<rcvDataBlock[i].size(); j++){
            if(newMarkRedBits[i][j]){
                cout << RED << rcvDataBlock[i][j] << RESET;
            }
            else{
                cout << rcvDataBlock[i][j];
            }
        }
        cout << endl;
    }
    cout << endl;
}



void printReceivedFrame(string receivedFrame, vector<bool> markedRedBits){
    cout << "received frame:" << endl;
    for(int i=0; i<receivedFrame.size(); i++){
        if(markedRedBits[i]){
            cout << RED << receivedFrame[i] << RESET ;
        }
        else{
            cout << receivedFrame[i];
        }
    }
    cout << endl;
    cout << endl;
}


void toggle(string &serializedData, vector<bool> &markRedBits, double p){
    std::mt19937 rng(std::random_device{}());
    std::uniform_real_distribution<double> dist(0.0, 1.0);

    double random_number = 0.0;
    for(int i=0; i<serializedData.size(); i++){
        random_number = dist(rng);
        if(random_number <= p && p!=0.0){
            markRedBits[i] = true;
            if(serializedData[i] == '0') serializedData[i] = '1';
            else if(serializedData[i] == '1') serializedData[i] = '0';
        }
    }


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