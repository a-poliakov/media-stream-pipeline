package ru.apolyakov.example.service.recognize;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class OpencvRecognizeFaceServiceImpl implements RecognizeFaceService {
//    @Value("processor.recognize.face.absolute-face-size:0")
    private Integer absoluteFaceSize = 0;

    private final CascadeClassifier haarFaceCascadeClassifier = new CascadeClassifier();
    private final CascadeClassifier lbpFaceCascadeClassifier = new CascadeClassifier();

    @PostConstruct
    public void init() throws IOException {
        // load the classifier(s)
        File haarClassifierResource = new ClassPathResource("haarcascades/haarcascade_frontalface_alt.xml").getFile();
        File lbpClassifierResource = new ClassPathResource("lbpcascades/lbpcascade_frontalface.xml").getFile();
        this.haarFaceCascadeClassifier.load(haarClassifierResource.getAbsolutePath());
        this.lbpFaceCascadeClassifier.load(lbpClassifierResource.getAbsolutePath());
    }

    @Override
    public Mat recognize(Mat frame) {
        // if the frame is not empty, process it
        if (!frame.empty())
        {
            // face detection
            CascadeClassifier faceCascade = lbpFaceCascadeClassifier;
            detectWithClassifier(frame, faceCascade);
        }
        return null;
    }

    /**
     * Method for face detection and tracking
     *
     * @param frame it looks for faces in this frame
     */
    private void detectWithClassifier(Mat frame, CascadeClassifier faceCascade)
    {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (this.absoluteFaceSize == 0)
        {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0)
            {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);

    }

}
