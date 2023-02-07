package ru.pelengator.model;

import ru.pelengator.Controller;

public class PdfExp {


    public enum TRIAL{
        TRIAL_MASSA("Проверка массы", "4.3.2"),
        TRIAL_GABARIT("Проверка габаритных размеров", "4.3.3"),
        TRIAL_RESISTANCE("Проверка переходного сопротивления", "4.3.4"),
        TRIAL_TIME_TEMP("Проверка времени выхода на рабочий режим и температуры криостатирования", "4.3.5"),
        TRIAL_ONE_FREQUENCY("Испытание на прочность  после воздействия  виброперегрузки на одной частоте", "4.3.16"),
        TRIAL_S_SH_W("Проверка устойчивости к воздействию СШВ","4.3.17"),
        TRIAL_WORK_TIME("Проверка времени непрерывной работы", "4.3.13"),

        TRIAL_INVALID_BAUD_RATE("Проверка кадровой частоты: полный кадр", "4.3.6"),
        TRIAL_DEVICE_NOT_OPENED_FOR_ERASE("Проверка кадровой частоты: окно", "4.3.6"),
        TRIAL_DEVICE_NOT_OPENED_FOR_WRITE("Проверка среднего значения пороговой облучённости ФЧЭ", "4.3.8"),
        TRIAL_FAILED_TO_WRITE_DEVICE("Проверка разброса вольтовой чувствительносит", "4.3.9"),
        TRIAL_EEPROM_READ_FAILED("Проверка распределения дефектных пикселей по матрице в цетральной зоне", "4.3.10"),
        TRIAL_EEPROM_WRITE_FAILED("Проверка распределения дефектных пикселей по матрице в зоне диаметром 140 элементов", "4.3.10"),
        TRIAL_EEPROM_ERASE_FAILED("Проверка количества кластеров дефектных элементов", "4.3.11"),
        TRIAL_EEPROM_NOT_PRESENT("Проверка динамического диапазона выходных сигналов", "4.3.7"),
        TRIAL_EEPROM_NOT_PROGRAMMED("FT_EEPROM_NOT_PROGRAMMED", metPoint),
        TRIAL_EEPROM_NOT_PROGRAMMED("FT_EEPROM_NOT_PROGRAMMED", metPoint),



        FT_INVALID_ARGS("FT_INVALID_ARGS", metPoint),
        FT_NOT_SUPPORTED("FT_NOT_SUPPORTED", metPoint),
        FT_NO_MORE_ITEMS("FT_NO_MORE_ITEMS", metPoint),
        FT_TIMEOUT("FT_TIMEOUT", metPoint),
        FT_OPERATION_ABORTED("FT_OPERATION_ABORTED", metPoint),
        FT_RESERVED_PIPE("FT_RESERVED_PIPE", metPoint),
        FT_INVALID_CONTROL_REQUEST_DIRECTION("FT_INVALID_CONTROL_REQUEST_DIRECTION", metPoint),
        FT_INVALID_CONTROL_REQUEST_TYPE("FT_INVALID_CONTROL_REQUEST_TYPE", metPoint),
        FT_IO_PENDING("FT_IO_PENDING", metPoint),
        FT_IO_INCOMPLETE("FT_IO_INCOMPLETE", metPoint),
        FT_HANDLE_EOF("FT_HANDLE_EOF", metPoint),
        FT_BUSY("FT_BUSY", metPoint),
        FT_NO_SYSTEM_RESOURCES("FT_NO_SYSTEM_RESOURCES", metPoint),
        FT_DEVICE_LIST_NOT_READY("FT_DEVICE_LIST_NOT_READY", metPoint),
        FT_DEVICE_NOT_CONNECTED("FT_DEVICE_NOT_CONNECTED", metPoint),
        FT_INCORRECT_DEVICE_PATH("FT_INCORRECT_DEVICE_PATH", metPoint),
        FT_OTHER_ERROR("FT_OTHER_ERROR", metPoint),

        FT_MY_ERROR("MY_ERROR", metPoint),
        FT_TESTING("MY_TESTING", metPoint);

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


    public static void loadExp(Controller controller) {
    }

    public static void SaveExp(Controller controller) {
    }
}
