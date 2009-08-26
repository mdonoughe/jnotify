################################################################################
# Automatically-generated file. Do not edit!
################################################################################

ROOT := ..

-include $(ROOT)/makefile.init

RM := rm -rf

# All of the sources participating in the build are defined here
-include sources.mk
-include $(SUBDIRS:%=%/subdir.mk)
-include objects.mk
ifneq ($(strip $(DEPS)),)
-include $(DEPS)
endif

-include $(ROOT)/makefile.defs

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: jnotify.dll

# Tool invocations
jnotify.dll: $(OBJS) $(USER_OBJS)
	@echo 'Building target: $@'
	@echo 'Invoking: GCC C++ Linker'
	@echo i586-mingw32msvc-g++ -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -shared -ojnotify.dll $(OBJS) $(USER_OBJS) $(LIBS)
	@i586-mingw32msvc-g++ -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -shared -ojnotify.dll $(OBJS) $(USER_OBJS) $(LIBS)
	@echo 'Finished building target: $@'
	@echo ' '

# Other Targets
clean:
	-$(RM) $(OBJS)$(DEPS)$(LIBRARIES) jnotify.dll
	-@echo ' '

.PHONY: all clean dependents
.SECONDARY:

-include $(ROOT)/makefile.targets
