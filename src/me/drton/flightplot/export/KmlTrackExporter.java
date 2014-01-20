package me.drton.flightplot.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by ada on 23.12.13.
 */
public class KmlTrackExporter extends AbstractTrackExporter implements TrackExporter, FlightModeChangeListener{

    private KmlTrackExportWriter writer;
    private boolean trackStarted = false;
    private KmlExportConfiguration config;

    public KmlTrackExporter(TrackReader trackReader) {
        super(trackReader);
        this.config = new KmlExportConfiguration();
    }

    public void exportToFile(File file, String title) throws IOException {
        Writer fileWriter = initWriter(file, title);
        try {
            this.writer.writeStart();

            TrackPoint point = readNextPoint();
            while (null != point) {
                this.writer.writePoint(point);
                point = readNextPoint();
            }

            this.writer.endTrackPart();
            this.writer.writeEnd();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fileWriter.close();
        }
    }

    private Writer initWriter(File file, String title) throws IOException {
        Writer fileWriter = new FileWriter(file);
        this.writer = new KmlTrackExportWriter(fileWriter, title);
        this.trackStarted = false;
        return fileWriter;
    }

    @Override
    public void flightModeChanged(FlightMode newFlightMode) {
        try{
            splitTrack(newFlightMode);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void splitTrack(FlightMode newFlightMode) throws IOException {
        if(config.isSplitTracksByFlightMode()){
            if(this.trackStarted){
                this.writer.endTrackPart();
                this.writer.startTrackPart(determineStyleByFlightMode(newFlightMode));
            }
            else {
                this.writer.startTrackPart(determineStyleByFlightMode(newFlightMode));
                this.trackStarted = true;
            }
        }
        else {
            if(!this.trackStarted){
                this.writer.startTrackPart();
                this.trackStarted = true;
            }
        }
    }

    protected String determineStyleByFlightMode(FlightMode flightMode){
        if(null == flightMode) {
            return KmlTrackExportWriter.LINE_STYLE_YELLOW;
        }

        switch(flightMode){
            case AUTO:
                return KmlTrackExportWriter.LINE_STYLE_RED;
            case STABILIZED:
                return KmlTrackExportWriter.LINE_STYLE_BLUE;
            case MANUAL:
            default:
                return KmlTrackExportWriter.LINE_STYLE_YELLOW;
        }
    }

    @Override
    public boolean configureExporter() {
        KmlExportConfigurationDialog dialog = new KmlExportConfigurationDialog();
        dialog.pack();
        dialog.setVisible(true);
        if(dialog.isCanceled()){
            return false;
        }
        else {
            this.config = dialog.getConfiguration();
            return true;
        }
    }
}
