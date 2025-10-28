package com.proposalmanagement.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        
        // Configure strict matching strategy to avoid unexpected mappings
        mapper.getConfiguration()
              .setMatchingStrategy(org.modelmapper.convention.MatchingStrategies.STRICT);
        
        return mapper;
    }
}