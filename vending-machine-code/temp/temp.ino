/* http://www.electronoobs.com */

/*-----( Inport library )-----*/
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
//i2c pins
LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE); // 

void setup() 
{
//WE define our LCD 16 columns and 2 rows
lcd.begin(16,2);
lcd.backlight();//Power on the back light
//lcd.backlight(); Power off the back light

}

void loop() 
{
//Write your text:
lcd.setCursor(0,0); //we start writing from the first row first column
lcd.print(" ELECTRONOOBS"); //16 characters poer line
delay(1000);//Delay used to give a dinamic effect
lcd.setCursor(0,1);
lcd.print("Thanks, share");
delay(8000); 

lcd.clear();//Clean the screen
lcd.setCursor(0,0); 
lcd.print(" How r U?");
lcd.setCursor(0,1);
lcd.print(" ELECTRONOOBS");
delay(8000); 
}
