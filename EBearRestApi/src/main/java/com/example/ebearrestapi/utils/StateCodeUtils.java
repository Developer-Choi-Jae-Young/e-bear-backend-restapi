    package com.example.ebearrestapi.utils;

    import com.example.ebearrestapi.etc.StateCode;
    import com.example.ebearrestapi.service.StateCodeService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.stereotype.Component;

    @Component
    @RequiredArgsConstructor
    public class StateCodeUtils implements CommandLineRunner {
        private final StateCodeService stateCodeService;

        @Override
        public void run(String... args) throws Exception {
            StateCode[] stateCodes = StateCode.values();
            for (StateCode stateCode : stateCodes) {
                stateCodeService.save(stateCode.getValue(), stateCode.getName());
            }
        }
    }
