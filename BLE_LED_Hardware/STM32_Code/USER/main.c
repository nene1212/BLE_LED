#include "sys.h"
#include "delay.h"
#include "usart.h"
#include "led.h"
#include "pwm.h"

int Pow(u16 a,u8 b){
	int i = 0;
	if(b == 0){
		return 1;
	}
	else{
		b--;
		for(i=0;i<b;i++){
			a *= a;
	}
	}
	return a;
}
//读数据
void Ble_Rdata(u16 *R, u16 *G , u16 *B){
	int len,i,j;
	
	if(USART_RX_STA&0x8000){
		
		len=USART_RX_STA&0x3fff;
		*R = 0;
		*G = 0;
		*B = 0;
		j = len;
		/*for(i=3;i>0;i--){
			*R += (USART_RX_BUF[i]-'0')*Pow(10,i);
			*G += (USART_RX_BUF[i+3]-'0')*Pow(10,i);
			*B += (USART_RX_BUF[i+6]-'0')*Pow(10,i);
			
		}*/

		while(USART_RX_BUF[j] != ' '){
			*B += (USART_RX_BUF[j]-'0')*Pow(10,len-j-1);
			j--;
		}
		i = len-j;
		j--;
		while(USART_RX_BUF[j] != ' '){
			*G += (USART_RX_BUF[j]-'0')*Pow(10,len-j-1-i);
			j--;
		}
		i = len-j;
		j--;
		while(j>=0){
			*R += (USART_RX_BUF[j]-'0')*Pow(10,len-j-i-1);
			j--;
		}
		USART_RX_STA=0;
	}
}

int main(void)
 {	
	u16 R,G,B;
	u8 i=0;
	u16 j=0;
	u8 time =0;
	R = 0;
	G = 0;
    B = 0;
	delay_init();	    	 //延时函数初始化	 
	delay_ms(500);

	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2); 	
	uart_init(9600);	 //串口初始化为96000
 	LED_Init();			     //LED端口初始化
 	TIM3_PWM_Init(999,0);	 //PWM频率
	while(!GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_8)){
			for(i=0;i<5;i++){
				if(GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_8))
					break;
				//呼吸灯
				for(j = 0;j<650;j++){
					delay_ms(5);
					switch(i){
						case 0:TIM_SetCompare2(TIM3,j);break;
						case 1:TIM_SetCompare2(TIM3,j);TIM_SetCompare3(TIM3,j);break;
						case 2:TIM_SetCompare3(TIM3,j);break;
						case 3:TIM_SetCompare2(TIM3,j);TIM_SetCompare1(TIM3,j);break;
						case 4:TIM_SetCompare3(TIM3,j);TIM_SetCompare1(TIM3,j);break;
					}
					
				}
				for(j = 650;j>0;j--){
					delay_ms(5);
					switch(i){
						case 0:TIM_SetCompare2(TIM3,j);break;
						case 1:TIM_SetCompare2(TIM3,j);TIM_SetCompare3(TIM3,j);break;
						case 2:TIM_SetCompare3(TIM3,j);break;
						case 3:TIM_SetCompare2(TIM3,j);TIM_SetCompare1(TIM3,j);break;
						case 4:TIM_SetCompare3(TIM3,j);TIM_SetCompare1(TIM3,j);break;
					}
				}
			}
			time=1;
		}
		if(time==1){
			__set_FAULTMASK(1);
			NVIC_SystemReset();
		}

	printf("nene1212 / Kagiri \r\n");
   	while(1)
	{

		Ble_Rdata(&R,&G,&B);
		
		TIM_SetCompare2(TIM3,B);	
		TIM_SetCompare1(TIM3,R);	
		TIM_SetCompare3(TIM3,G);	
		//连接后重启
		if(!GPIO_ReadInputDataBit(GPIOB,GPIO_Pin_8)){
			__set_FAULTMASK(1);
			NVIC_SystemReset();
		}

	}	 
 }

