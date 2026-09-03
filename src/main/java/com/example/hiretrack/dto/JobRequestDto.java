package com.example.hiretrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestDto{
        @NotBlank(message = "Title is required")
        @Size(min = 3,max = 50 ,message = "Title must be 3-50 characters")
        private String title;

        private String description;

        @Size(max = 50,message = "Department must be under 50 character")
        private String department;
 }
