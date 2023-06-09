package org.otrujillo.junit5app.ejemplos.models;

import jdk.jfr.Enabled;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.otrujillo.junit5app.ejemplos.exception.DineroInsuficienteException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CuentaTest {

    Cuenta cuenta;

    @BeforeEach
    void initMetodoTest() {
        this.cuenta = new Cuenta("Oscar", new BigDecimal("1000.12345"));

        System.out.println("Iniciando el método.");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Finalizando método del programa.");
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("Inicializando el test.");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Finalizando el test.");
    }

    @Nested
    @DisplayName("Probando atributos de cuenta")
    class CuentaTestNombreSaldo{
        @Test
        @DisplayName("Probando el saldo de la cuenta corriente, que no sea null, mayor que cero, valor esperado.")
        void testSaldoCuentaDev() {
            boolean esDev = "dev".equals(System.getProperty("ENV"));
            assumingThat(esDev, ()->{
                assertNotNull(cuenta.getSaldo());
                assertEquals(1000.12345, cuenta.getSaldo().doubleValue());

                assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0); //Asegurar que el saldo sea cero o mayor a cero, nunca negativo
                assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
            });
            ;
        }
    }

    @Nested
    class CuentaOperacionesTest{
        @Test
        void testDebitoCuenta() {
            cuenta.debito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.12345", cuenta.getSaldo().toPlainString());
        }

        @Test
        void testCreditoCuenta() {
            cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
        }

        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Oscar", new BigDecimal("1500.8989"));

            Banco banco = new Banco();
            banco.setNombre("Banco del Estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500)); //origen = suma, destino = resta
            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }



    @Test
    void testDineroInsuficienteExceptionCuenta() {
        this.cuenta = new Cuenta("Oscar", new BigDecimal("1000.12345"));

        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            cuenta.debito(new BigDecimal(1500));
        });

        String actual = exception.getMessage();
        String esperado = "Dinero insuficiente";
        assertEquals(esperado, actual);

    }



    @Test
    @DisplayName("Probando relaciones entre las cuentas y el banco con assertAll")
    //@Disabled
    void testRelacionBancoCuentas() {
        //fail();
        Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Oscar", new BigDecimal("1500.8989"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);
        banco.setNombre("Banco del Estado");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500)); //origen = suma, destino = resta
        assertAll(
                () -> assertEquals("1000.8989", cuenta2.getSaldo().toPlainString(),
                        () -> "El valor del saldo de la cuenta2 no es el esperado."),
                () -> assertEquals("3000", cuenta1.getSaldo().toPlainString(),
                        () -> "El valor del saldo de la cuenta1 no es el esperado."),
                () -> assertEquals(2, banco.getCuentas().size(),
                        () -> "El banco no tiene las cuentas esperadas."),
                () -> assertEquals("Banco del Estado", cuenta1.getBanco().getNombre()),
                () -> {
                    assertEquals("Oscar", banco.getCuentas().stream()
                            .filter(c -> c.getPersona().equals("Oscar"))
                            .findFirst()
                            .get().getPersona());
                },
                () -> {
                    assertTrue(banco.getCuentas().stream()
                            .anyMatch(c -> c.getPersona().equals("Oscar")));
                },
                () -> {
                    assertTrue(banco.getCuentas().stream()
                            .filter(c -> c.getPersona().equals("Oscar"))
                            .findFirst().isPresent());
                }
        );


    }

    @Nested
    class SistemaOperativoTest{
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {

        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testSoloLinuxMac() {

        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class JavaVersionTest{
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void soloJdk8() {
        }

        @Test
        @EnabledOnJre(JRE.JAVA_15)
        void soloJdk15(){}

        @Test
        @EnabledOnJre(JRE.JAVA_17)
        void soloJdk17(){}

        @Test
        @DisabledOnJre(JRE.JAVA_15)
        void testNoJdk15(){}
    }

    @Nested
    class SystemPropertiesTest{
        @Test
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v)-> System.out.println(k + ": " + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = ".*17.*")
        void testJavaVersion(){}

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testSolo64(){}

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testNo64(){}

        @Test
        @EnabledIfSystemProperty(named = "user.name", matches = "otrujillo")
        void testUserName(){}

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testDev(){}
    }

    @Nested
    class VariableAmbienteTest{
        @Test
        void imprimirVariablesAmbiente(){
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k, v)-> System.out.println(k + " = " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-17.0.1")
        void testJavaHome(){}

        @Test
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "12")
        void testProcesadores(){}

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void testEnv(){}

        @Test
        @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "prod")
        void testEnvProdDisabled(){}
    }

    @DisplayName("Probando DebitoCuenta repetido!")
    @RepeatedTest(value = 5, name = "{displayName} - Repetición número {currentRepetition} de {totalRepetitions}")
    void RepeatedTestDebitoCuenta(RepetitionInfo info) {
        if(info.getCurrentRepetition() == 3){
            System.out.println("Estamos en la repetición " + info.getCurrentRepetition());
        }
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} - {argumentsWithNames}")
    @ValueSource(doubles = {100, 200, 300, 500, 700, 1000})
    void testDebitoCuenta(double monto) {
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

}