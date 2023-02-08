package ru.pelengator.model;

import ru.pelengator.Controller;

public class PdfExp {


    public enum TRIAL {

        TRIAL_MASSA("Проверка массы", "4.3.2"),
        TRIAL_GABARIT("Проверка габаритных размеров", "4.3.3"),
        TRIAL_RESISTANCE("Проверка переходного сопротивления", "4.3.4"),
        TRIAL_TIME_TEMP("Проверка времени выхода на рабочий режим и температуры криостатирования", "4.3.5"),
        TRIAL_ONE_FREQUENCY("Испытание на прочность  после воздействия  виброперегрузки на одной частоте", "4.3.16"),
        TRIAL_S_SH_W("Проверка устойчивости к воздействию СШВ", "4.3.17"),
        TRIAL_WORK_TIME("Проверка времени непрерывной работы", "4.3.13"),

        TRIAL_FRAME_RATE_FULL_WINDOW("Проверка кадровой частоты: полный кадр", "4.3.6"),
        TRIAL_FRAME_RATE_SMALL_WINDOW("Проверка кадровой частоты: окно", "4.3.6"),
        TRIAL_EXPOSURE("Проверка среднего значения пороговой облучённости ФЧЭ", "4.3.8"),
        TRIAL_VW("Проверка разброса вольтовой чувствительносит", "4.3.9"),
        TRIAL_DEFF_PIXELS_CENTER("Проверка распределения дефектных пикселей по матрице в цетральной зоне", "4.3.10"),
        TRIAL_DEFF_PIXELS_DIAMETR("Проверка распределения дефектных пикселей по матрице в зоне диаметром 140 элементов", "4.3.10"),
        TRIAL_CLUSTER("Проверка количества кластеров дефектных элементов", "4.3.11"),
        TRIAL_DINAM_DIAP("Проверка динамического диапазона выходных сигналов", "4.3.7");

        TRIAL(String expName, String metPoint) {
            this.expName = expName;
            this.metPoint = metPoint;
        }
        public final String expName;
        public final String metPoint;

        public String getMetPoint() {
            return metPoint;
        }
        public String getExpName() {
            return this.expName;
        }
    }

    public enum CLIMAT {

        CLIMAT_NCU("НКУ"),
        CLIMAT_MINUS("-55 \u2103"),
        CLIMAT_PLUS("+60 \u2103"),
        CLIMAT_CICLE(" от -55 \u2103 до +60 \u2103");

        CLIMAT(String name) {
            this.name = name;
        }
        public final String name;
        public String getName() {
            return this.name;
        }
    }

    public enum PRESSURE {

        PRESSURE_NORMAL("29.0 МПа"),
        PRESSURE_HI("33.0 МПа"),
        PRESSURE_LOW("22.0 МПа"),
        PRESSURE_BUTTOM("12.0 МПа");

        PRESSURE(String expName) {
            this.name = expName;
        }
        public final String name;
        public String getName() {
            return this.name;
        }
    }



    public static void loadExp(Controller controller) {
    }

    public static void saveExp(Controller controller) {
    }

    public static void updateExp(Controller controller) {
    }
}
