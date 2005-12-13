################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
$(ROOT)/Lock.cpp \
$(ROOT)/Logger.cpp \
$(ROOT)/WatchData.cpp \
$(ROOT)/Win32FSHook.cpp \
$(ROOT)/net_contentobjects_jnotify_win32_JNotify_win32.cpp 

OBJS += \
./Lock.o \
./Logger.o \
./WatchData.o \
./Win32FSHook.o \
./net_contentobjects_jnotify_win32_JNotify_win32.o 

DEPS += \
${addprefix ./, \
Lock.d \
Logger.d \
WatchData.d \
Win32FSHook.d \
net_contentobjects_jnotify_win32_JNotify_win32.d \
}


# Each subdirectory must supply rules for building sources it contributes
%.o: $(ROOT)/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	@echo g++ -I"X:\bin\jdk\1.5.0_04\include" -I"X:\bin\jdk\1.5.0_04\include\win32" -O0 -Wall -c -fmessage-length=0 -o$@ $<
	@g++ -I"X:\bin\jdk\1.5.0_04\include" -I"X:\bin\jdk\1.5.0_04\include\win32" -O0 -Wall -c -fmessage-length=0 -o$@ $< && \
	echo -n $(@:%.o=%.d) $(dir $@) > $(@:%.o=%.d) && \
	g++ -MM -MG -P -w -I"X:\bin\jdk\1.5.0_04\include" -I"X:\bin\jdk\1.5.0_04\include\win32" -O0 -Wall -c -fmessage-length=0  $< >> $(@:%.o=%.d)
	@echo 'Finished building: $<'
	@echo ' '


