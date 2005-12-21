################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
$(ROOT)/net_contentobjects_jnotify_linux_JNotify_linux.c 

OBJS += \
./net_contentobjects_jnotify_linux_JNotify_linux.o 

DEPS += \
${addprefix ./, \
net_contentobjects_jnotify_linux_JNotify_linux.d \
}


# Each subdirectory must supply rules for building sources it contributes
%.o: $(ROOT)/%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	@echo gcc -I/usr/local/jdk1.5.0_04/include/ -I/usr/local/jdk1.5.0_04/include//linux -O0 -g3 -Wall -c -fmessage-length=0 -o$@ $<
	@gcc -I/usr/local/jdk1.5.0_04/include/ -I/usr/local/jdk1.5.0_04/include//linux -O0 -g3 -Wall -c -fmessage-length=0 -o$@ $< && \
	echo -n $(@:%.o=%.d) $(dir $@) > $(@:%.o=%.d) && \
	gcc -MM -MG -P -w -I/usr/local/jdk1.5.0_04/include/ -I/usr/local/jdk1.5.0_04/include//linux -O0 -g3 -Wall -c -fmessage-length=0  $< >> $(@:%.o=%.d)
	@echo 'Finished building: $<'
	@echo ' '


