#include<EEPROM.h>
#include<Keypad.h>
#include<LiquidCrystal_I2C.h>
#include<Wire.h>

//------PGCODE|MID--------
#define PGCODE_ADDRESS 0
#define MID_ADDRESS 50
#define PINSTACK_ADDRESS 500

int pgcode;
String mid;
//------------------------

//---------KEYPAD----------

const byte ROWS = 4; 
const byte COLS = 3; 

char hexa_keys[ROWS][COLS] = {
  {'1', '2', '3'},
  {'4', '5', '6'},
  {'7', '8', '9'},
  {'*', '0', '#'}};

byte row_pins[ROWS] = {9, 8, 7, 6}; 
byte col_pins[COLS] = {5, 4, 3}; 

Keypad custom_keypad = Keypad(makeKeymap(hexa_keys), row_pins, col_pins, ROWS, COLS);
String keypad_pin_input;
int current_key_index = 0;
//----------------------------

//----------LCD--------------

LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);

//-----------------------------


char hex[256];
uint8_t data[256];
int start = 0;
int seconds = 0;
uint8_t hash[32];
String pin;
#define SHA256_BLOCK_SIZE 32

typedef struct {
  uint8_t data[64];
  uint32_t datalen;
  unsigned long long bitlen;
  uint32_t state[8];
} SHA256_CTX;

void sha256_init(SHA256_CTX *ctx);
void sha256_update(SHA256_CTX *ctx, const uint8_t data[], size_t len);
void sha256_final(SHA256_CTX *ctx, uint8_t hash[]);

#define ROTLEFT(a,b) (((a) << (b)) | ((a) >> (32-(b))))
#define ROTRIGHT(a,b) (((a) >> (b)) | ((a) << (32-(b))))

#define CH(x,y,z) (((x) & (y)) ^ (~(x) & (z)))
#define MAJ(x,y,z) (((x) & (y)) ^ ((x) & (z)) ^ ((y) & (z)))
#define EP0(x) (ROTRIGHT(x,2) ^ ROTRIGHT(x,13) ^ ROTRIGHT(x,22))
#define EP1(x) (ROTRIGHT(x,6) ^ ROTRIGHT(x,11) ^ ROTRIGHT(x,25))
#define SIG0(x) (ROTRIGHT(x,7) ^ ROTRIGHT(x,18) ^ ((x) >> 3))
#define SIG1(x) (ROTRIGHT(x,17) ^ ROTRIGHT(x,19) ^ ((x) >> 10))

static const uint32_t k[64] = {
  0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,
  0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,
  0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
  0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,
  0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,
  0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,
  0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
  0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2
};

void sha256_transform(SHA256_CTX *ctx, const uint8_t data[]) {
  uint32_t a, b, c, d, e, f, g, h, i, j, t1, t2, m[64];

  for (i = 0, j = 0; i < 16; ++i, j += 4)
    m[i] = ((uint32_t)data[j] << 24) | ((uint32_t)data[j + 1] << 16) | ((uint32_t)data[j + 2] << 8) | ((uint32_t)data[j + 3]);
  for ( ; i < 64; ++i)
    m[i] = SIG1(m[i - 2]) + m[i - 7] + SIG0(m[i - 15]) + m[i - 16];

  a = ctx->state[0];
  b = ctx->state[1];
  c = ctx->state[2];
  d = ctx->state[3];
  e = ctx->state[4];
  f = ctx->state[5];
  g = ctx->state[6];
  h = ctx->state[7];

  for (i = 0; i < 64; ++i) {
    t1 = h + EP1(e) + CH(e,f,g) + k[i] + m[i];
    t2 = EP0(a) + MAJ(a,b,c);
    h = g;
    g = f;
    f = e;
    e = d + t1;
    d = c;
    c = b;
    b = a;
    a = t1 + t2;
  }

  ctx->state[0] += a;
  ctx->state[1] += b;
  ctx->state[2] += c;
  ctx->state[3] += d;
  ctx->state[4] += e;
  ctx->state[5] += f;
  ctx->state[6] += g;
  ctx->state[7] += h;
}

void sha256_init(SHA256_CTX *ctx)
{
  ctx->datalen = 0;
  ctx->bitlen = 0;
  ctx->state[0] = 0x6a09e667;
  ctx->state[1] = 0xbb67ae85;
  ctx->state[2] = 0x3c6ef372;
  ctx->state[3] = 0xa54ff53a;
  ctx->state[4] = 0x510e527f;
  ctx->state[5] = 0x9b05688c;
  ctx->state[6] = 0x1f83d9ab;
  ctx->state[7] = 0x5be0cd19;
}

void sha256_update(SHA256_CTX *ctx, const uint8_t data[], size_t len) {
  uint32_t i;

  for (i = 0; i < len; ++i) {
    ctx->data[ctx->datalen] = data[i];
    ctx->datalen++;
    if (ctx->datalen == 64) {
      sha256_transform(ctx, ctx->data);
      ctx->bitlen += 512;
      ctx->datalen = 0;
    }
  }
}

void sha256_final(SHA256_CTX *ctx, uint8_t hash[]) {
  uint32_t i;

  i = ctx->datalen;

  // Pad whatever data is left in the buffer.
  if (ctx->datalen < 56) {
    ctx->data[i++] = 0x80;
    while (i < 56)
      ctx->data[i++] = 0x00;
  }
  else {
    ctx->data[i++] = 0x80;
    while (i < 64)
      ctx->data[i++] = 0x00;
    sha256_transform(ctx, ctx->data);
    memset(ctx->data, 0, 56);
  }

  // Append to the padding the total message's length in bits and transform.
  ctx->bitlen += ctx->datalen * 8;
  ctx->data[63] = ctx->bitlen;
  ctx->data[62] = ctx->bitlen >> 8;
  ctx->data[61] = ctx->bitlen >> 16;
  ctx->data[60] = ctx->bitlen >> 24;
  ctx->data[59] = ctx->bitlen >> 32;
  ctx->data[58] = ctx->bitlen >> 40;
  ctx->data[57] = ctx->bitlen >> 48;
  ctx->data[56] = ctx->bitlen >> 56;
  sha256_transform(ctx, ctx->data);

  // Since this implementation uses little endian byte ordering and SHA uses big endian,
  // reverse all the bytes when copying the final state to the output hash.
  for (i = 0; i < 4; ++i) {
    hash[i]      = (ctx->state[0] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 4]  = (ctx->state[1] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 8]  = (ctx->state[2] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 12] = (ctx->state[3] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 16] = (ctx->state[4] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 20] = (ctx->state[5] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 24] = (ctx->state[6] >> (24 - i * 8)) & 0x000000ff;
    hash[i + 28] = (ctx->state[7] >> (24 - i * 8)) & 0x000000ff;
  }
}

char *btoh(char *dest, uint8_t *src, int len) {
  char *d = dest;
  while( len-- ) sprintf(d, "%02x", (unsigned char)*src++), d += 2;
  return dest;
}

String SHA256(String data) 
{
  uint8_t data_buffer[data.length()];
  
  for(int i=0; i<data.length(); i++)
  {
    data_buffer[i] = (uint8_t)data.charAt(i);
  }
  
  SHA256_CTX ctx;
  ctx.datalen = 0;
  ctx.bitlen = 512;
  
  sha256_init(&ctx);
  sha256_update(&ctx, data_buffer, data.length());
  sha256_final(&ctx, hash);
  return(btoh(hex, hash, 32));
}

//-1 => rejected else offset from pgcode
int checkPin(String mid, int pgcode, String pin, int limit)
{
  String sha;
  String pins_skipped[10];
  bool success = false;
  int pgc_skipped = 0;
  for(int i = pgcode; i<pgcode +limit; i++)
  {
    sha = SHA256(String(i) + mid);
    String s_pin = sha.substring(0, 6);
    s_pin.toUpperCase();
    String i_pin = "";
    for(int j = 0; j <= 5; j++)
    {
      int ascii = s_pin.charAt(j);
      i_pin += (String)(ascii % 10);
    }
    if(i_pin.equals(pin))
    {
      success = true;
      break;
    }
    pins_skipped[pgc_skipped] = i_pin;
    pgc_skipped++;
  }
  
  if(success == false)
  {
    success = checkIfPinPresentInStack(pin);
    if(success)
    {
      return(limit+1);
    }
    pgc_skipped = 0;
    return(-1);
  }
  else 
  {
    for(int i = 0; i<pgc_skipped; i++)
    {
      addPinToStack(pins_skipped[i]);
    }
    return(pgc_skipped);
  } 
}

void storeStringToAddress(String data_string, int address, char end_char = '*')
{
  Serial.println("");
  Serial.print("<-");
  int len = data_string.length();
  int curr_char_address;
  
  for(int i = 0; i<len; i++)
  {
    curr_char_address = address + i;
    EEPROM.write(curr_char_address, data_string.charAt(i));
    Serial.print(data_string.charAt(i));
  }
  Serial.println("");
  EEPROM.write(curr_char_address + 1, end_char);
}

String getStringFromAddress(int address, char end_char= '*')
{
  Serial.println("");
  Serial.print("->");
  String data;
  char curr_char;
  int curr_char_address;
  int i = 0;
  while(true) 
  {
    curr_char_address = address + i;
    curr_char = EEPROM.read(curr_char_address);
    if(curr_char != end_char)
    {
      data += (String)curr_char;
      Serial.print(curr_char);
    }
    else
    {
      Serial.println("");
      return(data);
    }
    i++;
  }
}

void addPinToStack(String pin)
{
  char sub_end_char = '*';
  char end_char = '/';
  String string = getStringFromAddress(PINSTACK_ADDRESS, end_char);
  string += sub_end_char;
  string += pin;
  string += end_char;
  storeStringToAddress(string, PINSTACK_ADDRESS, end_char);
}

void removePinFromStack(String pin)
{
  String string_array[10];
  char sub_end_char = '*';
  char end_char = '/';
  String string = getStringFromAddress(PINSTACK_ADDRESS, end_char);
  string += end_char;
  
  int j = 0;
  int i = 0;
  bool end_flag = false;
  while(true)
  {
    String sub_string = "";
    while(true)
    {
      char ch = string.charAt(j);
      j++;
      if(ch == sub_end_char)
      {
        break;
      }
      if(ch == end_char)
      {
        end_flag = true;
        break;
      }
      sub_string += String(ch);
    }
    if(!pin.equals(sub_string))
    {
      string_array[i] = sub_string;
      Serial.println(sub_string);
      i++;
    }
    else
    {
      Serial.println(pin + " is removed from stack");
    }
    if(end_flag)
    {
      String temp_string = "";
      for(int k = 0; k < i; k++)
      {
        temp_string += string_array[k];
        temp_string += "*";
      }
      temp_string.remove(temp_string.length()-1);
      temp_string += "/";
      storeStringToAddress(temp_string, PINSTACK_ADDRESS, end_char);
      return;
    }
  }
}

bool checkIfPinPresentInStack(String pin)
{
  char sub_end_char = '*';
  char end_char = '/';
  String string = getStringFromAddress(PINSTACK_ADDRESS, end_char);
  string+= end_char;
  Serial.println(string);
  
  int j = 0;
  bool end_flag = false;
  while(true)
  {
    String sub_string = "";
    while(true)
    {
      char ch = string.charAt(j);
      j++;
      if(ch == sub_end_char)
      {
        break;
      }
      if(ch == end_char)
      {
        end_flag = true;
        break;
      }
      sub_string += String(ch);
    }
    Serial.println(sub_string);
    if(pin.equals(sub_string))
    {
      removePinFromStack(pin);
      Serial.println("pin in stack");
      return(true);
    }
    if(end_flag)
    {
      Serial.println("pin not in stack");
      return(false);
    }
  }
}

void displayLCD(String msg)
{
  lcd.begin(16, 2);
  lcd.backlight();
  lcd.setCursor(0,0);
  lcd.print(msg);
}

void setup()
{
  Serial.begin(9600);
  storeStringToAddress("0", PGCODE_ADDRESS);
  pgcode = getStringFromAddress(PGCODE_ADDRESS).toInt();
  mid = getStringFromAddress(MID_ADDRESS);
  keypad_pin_input = "";

  storeStringToAddress("123456*212451*999038/", PINSTACK_ADDRESS, '/');
}

void loop()
{
  char key_pressed = custom_keypad.getKey();
  bool pin_entered = false;
  if (Serial.available() > 0){
    if(current_key_index < 6 && key_pressed!='#' && key_pressed != '*')
    {
      keypad_pin_input += String(key_pressed);
      current_key_index++;
    }
    if(current_key_index == 6 && key_pressed == '#')
    {
      current_key_index = 0;
      pin_entered = true;
    }
    if(key_pressed == '*')
    {
      if(current_key_index > 0)
      {
        current_key_index--;
        keypad_pin_input.remove(current_key_index);
      }
    }
    Serial.println(keypad_pin_input);
  }
  
  if (pin_entered)
  { 
    pin_entered = false;
    String pin = keypad_pin_input;
    keypad_pin_input = "";
    pin.remove(6);
    Serial.print("\n" + pin + " -> ");
    int limit = 10;
    
    int result = checkPin(mid, pgcode, pin, limit);
    
    if(result == -1)
    {
      Serial.println("Pin Rejected");
    }
    else
    {
      Serial.print("Pin Accepted");
      if(result < limit)
      {
        pgcode += (result+1);
        storeStringToAddress(String(pgcode), PGCODE_ADDRESS);
      }
    }
  }
  
}
