#include<xc.h>                      // processor SFR definitions
#include<sys/attribs.h>             // __ISR macro
#include<math.h>
#include "i2c_master_noint.h"       // I2C functions
#include "ILI9163C.h"               // TFTLCD functions

// DEVCFG0
#pragma config DEBUG = OFF          // no debugging
#pragma config JTAGEN = OFF         // no jtag
#pragma config ICESEL = ICS_PGx1    // use PGED1 and PGEC1
#pragma config PWP = OFF            // no write protect
#pragma config BWP = OFF            // no boot write protect
#pragma config CP = OFF             // no code protect

// DEVCFG1
#pragma config FNOSC = PRIPLL       // use primary oscillator with pll
#pragma config FSOSCEN = OFF        // turn off secondary oscillator
#pragma config IESO = OFF           // no switching clocks
#pragma config POSCMOD = HS         // high speed crystal mode
#pragma config OSCIOFNC = OFF       // free up secondary osc pins
#pragma config FPBDIV = DIV_1       // divide CPU freq by 1 for peripheral bus clock
#pragma config FCKSM = CSDCMD       // do not enable clock switch
#pragma config WDTPS = PS1048576    // slowest wdt
#pragma config WINDIS = OFF         // no wdt window
#pragma config FWDTEN = OFF         // wdt off by default
#pragma config FWDTWINSZ = WINSZ_25 // wdt window at 25%

// DEVCFG2 - get the CPU clock to 48MHz
#pragma config FPLLIDIV = DIV_2     // divide input clock to be in range 4-5MHz
#pragma config FPLLMUL = MUL_24     // multiply clock after FPLLIDIV
#pragma config FPLLODIV = DIV_2     // divide clock after FPLLMUL to get 48MHz
#pragma config UPLLIDIV = DIV_2     // divider for the 8MHz input clock, then multiply by 12 to get 48MHz for USB
#pragma config UPLLEN = ON          // USB clock on

// DEVCFG3
#pragma config USERID = 0x1234      // some 16bit userid, doesn't matter what
#pragma config PMDL1WAY = OFF       // allow multiple reconfigurations
#pragma config IOL1WAY = OFF        // allow multiple reconfigurations
#pragma config FUSBIDIO = ON        // USB pins controlled by USB module
#pragma config FVBUSONIO = ON       // USB BUSON controlled by USB module

#define SLAVE_ADDR 0x6B             // slave address 01101011
#define maxLength 14                // slave address 01101011

unsigned char whoAmI  = 0x00;       // check who am I register 
char stuff[maxLength];              // save 14 8 bit data
short gx = 0x0000;                  // gyroscope x
short gy = 0x0000;                  // gyroscope x
short gz = 0x0000;                  // gyroscope x
short ax = 0x0000;                  // gyroscope x
short ay = 0x0000;                  // gyroscope x
short az = 0x0000;                  // gyroscope x
short temperature = 0x0000;         // gyroscope x

//function prototype
void initIMU();
unsigned char getWhoAmI();
void I2C_read_multiple(char add, char reg, char * data, char length);

/*
void __ISR(_TIMER_2_VECTOR, IPL5SOFT) PWMcontroller(void) { 
  
  IFS0bits.T2IF = 0;
}
*/

int main() {

    __builtin_disable_interrupts();

    // set the CP0 CONFIG register to indicate that kseg0 is cacheable (0x3)
    __builtin_mtc0(_CP0_CONFIG, _CP0_CONFIG_SELECT, 0xa4210583);

    // 0 data RAM access wait states
    BMXCONbits.BMXWSDRM = 0x0;

    // enable multi vector interrupts
    INTCONbits.MVEC = 0x1;

    // disable JTAG to get pins back
    DDPCONbits.JTAGEN = 0;
    
    // do your TRIS and LAT commands here
    TRISAbits.TRISA4 = 0;         //RA4 (PIN#12) for Green LED
    TRISBbits.TRISB4 = 1;         //RB4 (PIN#11) for pushbutton
    /*
    // for Timer2
    PR2 = 2999;                   // period = (PR2+1) * N * 20.8 ns = 0.001 s, 1 kHz
    TMR2 = 0;                     // initial TMR2 count is 0
    T2CONbits.TCKPS = 0b100;      // Timer2 prescaler N=16 (1:16)
    T2CONbits.ON = 1;             // turn on Timer2
    OC1CONbits.OCM = 0b110;       // PWM mode without fault pin; other OC1CON bits are defaults
    OC1CONbits.ON = 1;            // turn on OC1
    OC1R = 1500;
    OC1CONbits.OC32 = 0;
    OC1CONbits.OCTSEL = 0;        // select Timer2
    
    OC2CONbits.OCM = 0b110;       // PWM mode without fault pin; other OC1CON bits are defaults
    OC2CONbits.ON = 1;            // turn on OC2
    OC2R = 1500;
    OC2CONbits.OC32 = 0;
    OC2CONbits.OCTSEL = 0;        // select Timer2
    IPC2bits.T2IP = 5;            // step 4: interrupt priority
    IPC2bits.T2IS = 0;            // step 4: interrupt priority
    IFS0bits.T2IF = 0;            // step 5: clear the int flag
    IEC0bits.T2IE = 1;            // step 6: enable Timer2 by setting IEC0<11>
    */
    
    __builtin_enable_interrupts();
    
    // init I2C
    i2c_master_setup(); 
    initIMU();
    // init SPI
    SPI1_init();
    LCD_init();
    LCD_clearScreen(BLACK);     
    int x=0;
    int y=0;
    // TFTLCD
    //LCD_drawPixel(28, 32, BLACK); // set the x,y pixel to a color
    
    // check I2C communication
    if(getWhoAmI() == 0x69){
        LATAbits.LATA4 = 1;
    }
    
    while(1){
        _CP0_SET_COUNT(0);
        I2C_read_multiple(SLAVE_ADDR, 0x20, stuff, 14);
        temperature = ((stuff[1]<<8) | stuff[0]);
        gx = ((stuff[3]<<8) | stuff[2]);
        gy = ((stuff[5]<<8) | stuff[4]);
        gz = ((stuff[7]<<8) | stuff[6]);
        ax = ((stuff[9]<<8) | stuff[8]);
        ay = ((stuff[11]<<8) | stuff[10]);
        az = ((stuff[13]<<8) | stuff[12]);
        
        while(_CP0_GET_COUNT() < 480000) { // 50 Hz
            ;
        }
    }
}

void initIMU(){  
    // init accelerometer
    i2c_master_start();
    i2c_master_send(0xD6);    
    i2c_master_send(0x10);
    i2c_master_send(0x80);
    i2c_master_stop();
    
    // init gyroscope
    i2c_master_start();
    i2c_master_send(0xD6);    
    i2c_master_send(0x11);
    i2c_master_send(0x80);
    i2c_master_stop();
    
    // init CTRL3_C IF_CON bit
    i2c_master_start();
    i2c_master_send(0xD6);    
    i2c_master_send(0x12);
    i2c_master_send(0b00000100);
    i2c_master_stop();
}

unsigned char getWhoAmI(){
    i2c_master_start();
    i2c_master_send(0xD6);    
    i2c_master_send(0x0F);
    i2c_master_restart();
    i2c_master_send(0xD7);
    whoAmI = i2c_master_recv();
    i2c_master_ack(1);
    i2c_master_stop();
    
    return whoAmI;
}

void I2C_read_multiple(char add, char reg, char * data, char length){
    i2c_master_start();
    i2c_master_send(add << 1);    
    i2c_master_send(reg);
    i2c_master_restart();
    i2c_master_send((add << 1)|1);
    
    int i;
    for(i =0 ; i < (length-1); i++){
        data[i] = i2c_master_recv();
        i2c_master_ack(0);
    }
    
    data[length-1] = i2c_master_recv();
    i2c_master_ack(1);
    i2c_master_stop();
}