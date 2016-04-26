#include<xc.h>           // processor SFR definitions
#include<sys/attribs.h>  // __ISR macro
#include<math.h>
#include "i2c_master_noint.h"
// DEVCFG0
#pragma config DEBUG = OFF // no debugging
#pragma config JTAGEN = OFF // no jtag
#pragma config ICESEL = ICS_PGx1 // use PGED1 and PGEC1
#pragma config PWP = OFF // no write protect
#pragma config BWP = OFF // no boot write protect
#pragma config CP = OFF // no code protect

// DEVCFG1
#pragma config FNOSC = PRIPLL // use primary oscillator with pll
#pragma config FSOSCEN = OFF // turn off secondary oscillator
#pragma config IESO = OFF // no switching clocks
#pragma config POSCMOD = HS // high speed crystal mode
#pragma config OSCIOFNC = OFF // free up secondary osc pins
#pragma config FPBDIV = DIV_1 // divide CPU freq by 1 for peripheral bus clock
#pragma config FCKSM = CSDCMD // do not enable clock switch
#pragma config WDTPS = PS1048576  // slowest wdt
#pragma config WINDIS = OFF // no wdt window
#pragma config FWDTEN = OFF // wdt off by default
#pragma config FWDTWINSZ = WINSZ_25 // wdt window at 25%

// DEVCFG2 - get the CPU clock to 48MHz
#pragma config FPLLIDIV = DIV_2 // divide input clock to be in range 4-5MHz
#pragma config FPLLMUL = MUL_20 // multiply clock after FPLLIDIV
#pragma config FPLLODIV = DIV_2 // divide clock after FPLLMUL to get 48MHz
#pragma config UPLLIDIV = DIV_2 // divider for the 8MHz input clock, then multiply by 12 to get 48MHz for USB
#pragma config UPLLEN = ON // USB clock on

// DEVCFG3
#pragma config USERID = 0 // some 16bit userid, doesn't matter what
#pragma config PMDL1WAY = OFF // allow multiple reconfigurations
#pragma config IOL1WAY = OFF // allow multiple reconfigurations
#pragma config FUSBIDIO = ON // USB pins controlled by USB module
#pragma config FVBUSONIO = ON // USB BUSON controlled by USB module

// define constants and variables

#define Pi 3.1415926
#define SineCount 100
#define TriangleCount 200
#define CS LATBbits.LATB7 

static volatile float SineWave[100]; 
static volatile float TriangleWave[TriangleCount]; 

char read  = 0x00;
unsigned char checkGP7 = 0x00;

void MakeSineWave();
void maketrianglewave();
void InitSPI1();
void initI2C();
void initI2C2();

char SPI1_IO(char write);

void setVoltage(char channel, int voltage);
void setExpander(int pin, int level);
char getExpander();
unsigned char setLowBitOperation(int pin);



int main() {

   /* __builtin_disable_interrupts();
    // set the CP0 CONFIG register to indicate that kseg0 is cacheable (0x3)
    __builtin_mtc0(_CP0_CONFIG, _CP0_CONFIG_SELECT, 0xa4210583);
    // 0 data RAM access wait states
    BMXCONbits.BMXWSDRM = 0x0;
    // enable multi vector interrupts
    INTCONbits.MVEC = 0x1;
    // disable JTAG to get pins back
    DDPCONbits.JTAGEN = 0;    
    // do your TRIS and LAT commands here    
    __builtin_enable_interrupts();  
     // set up USER pin as input*/
    TRISBbits.TRISB4 = 1; // 0 for output, 1 for input
    // set up LED1 pin as a digital output   
    TRISAbits.TRISA4 = 0; // 0 for output, 1 for input
    LATAbits.LATA4 = 0; 
    
    InitSPI1();
	MakeSineWave();
    MakeTriangleWave();
	i2c_master_setup();
    initI2C(); 
    initI2C2();
  
    while(1) {
    i2c_master_start();
    i2c_master_send(0b11010110);    
    i2c_master_send(0x0F);
    i2c_master_restart();
    i2c_master_send(0b11010111);
    LATAbits.LATA4 = 0;
    read = i2c_master_recv();
    LATAbits.LATA4 = 0;
    i2c_master_ack(1);
    i2c_master_stop();
   
	if (read == 0b01101001){
	LATAbits.LATA4 = 1; 	
	}
 
	
		
	 
     }
     
    
    
    
}

//Library

void InitSPI1(){
	 TRISBbits.TRISB7 = 0b0;// B7 output pin
    CS = 1;                 // set B7 high
    SS1Rbits.SS1R = 0b0100;   //  SS1 = RB7
    SDI1Rbits.SDI1R = 0b0000; //  SDI1 = RA1 ?????
    RPB8Rbits.RPB8R = 0b0011; //  SDO1 = RB8
    ANSELBbits.ANSB14 = 0;    // turn off AN10 ???????
    
    // setup SPI1
    SPI1CON = 0;              // turn off the SPI1 module and reset it
    SPI1BUF;                  // clear the rx buffer by reading from it
    SPI1BRG = 0x1;            // baud rate to 12 MHz [SPI4BRG = (48000000/(2*desired))-1]
    SPI1STATbits.SPIROV = 0;  // clear the overflow bit
    SPI1CONbits.MODE32 = 0;   // use 8 bit mode
    SPI1CONbits.MODE16 = 0;
    SPI1CONbits.CKE = 1;      // data changes when clock goes from hi to lo (since CKP is 0)
    SPI1CONbits.MSTEN = 1;    // master operation
    SPI1CONbits.ON = 1;       //
	
}
void MakeSineWave(){
	int i;
	for(i=0; i < SineCount; i++){
		SineWave[i] = 127+128*sin(2*Pi*i*0.01);
	  
	}
	
}

void MakeTriangleWave(){
	int j;
	for(j=0; j < TriangleCount; j++){
		TriangleWave[j] = 255*(0.005*j);
		
	}
	
}


char SPI1_IO(char write){
	 SPI1BUF = write;
    while(!SPI1STATbits.SPIRBF) { // wait to receive the byte
     ;
     }
    return SPI1BUF; 
	
}



void setVoltage(char channel, int voltage){
	int temp = voltage;
    if(channel == 0) { // 0 for VoutA
        CS = 0; 
        SPI1_IO((temp >> 4) | 0b01110000); // 4 configuration bits
        SPI1_IO(temp << 4); // Data bits 
        CS = 1;   
    }
    if(channel == 1) { // 1 for VoutB
        CS = 0; 
        SPI1_IO((temp >> 4) | 0b11110000); // 4 configuration bits
        SPI1_IO(temp << 4); // Data bits
        CS = 1;   
    }
	
}


void setExpander(int pin, int level){
        
        getExpander();
        i2c_master_start();
        i2c_master_send(0x40);    
        i2c_master_send(0x0A);
        if(level == 1){
            i2c_master_send((1 << pin)|read);
        }
        if(level == 0){
            unsigned char temp;
            temp = setLowBitOperation(pin);
            i2c_master_send(read & temp);
        }
        i2c_master_stop();   
}

char getExpander(){
    i2c_master_start();
    i2c_master_send(0x40);    
    i2c_master_send(0x09);
    i2c_master_restart();
    i2c_master_send(0x41);
    read = i2c_master_recv();
    i2c_master_ack(1);
    i2c_master_stop();
    
    return read;
}

unsigned char setLowBitOperation(int pin){
    unsigned char b1=0xff;
    unsigned char b2, b3, b4;
    b2 = b1 << (pin+1);
    b3 = b1 >> (8-pin);
    b4 = b2 ^ b3;
    return b4;
}

void initI2C2(){
    ANSELBbits.ANSB2 = 0;
    ANSELBbits.ANSB3 = 0;
    i2c_master_setup();

    

}

void initI2C(){
    i2c_master_start();
    i2c_master_send(0b11010110);    
    i2c_master_send(0x10);
    i2c_master_send(0x80);
    i2c_master_stop();
	
	i2c_master_start();
    i2c_master_send(0b11010110);    
    i2c_master_send(0x11);
    i2c_master_send(0x80);
    i2c_master_stop();
	
	i2c_master_start();
    i2c_master_send(0b11010110);    
    i2c_master_send(0x12);
    i2c_master_send(0x04);
    i2c_master_stop();
	
}
