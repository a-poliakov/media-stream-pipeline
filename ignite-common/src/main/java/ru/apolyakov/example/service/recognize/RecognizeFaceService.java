package ru.apolyakov.example.service.recognize;

import org.opencv.core.Mat;

public interface RecognizeFaceService {
    public Mat recognize(Mat input);
}
