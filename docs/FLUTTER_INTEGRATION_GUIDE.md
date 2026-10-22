# MediSimbio Flutter Mobile App: Integration Guide & Screen Mapping

**Document Version**: 1.0.0 (Integration-Ready)  
**Location**: `D:\medsimbio\medisimbio-backend\docs\FLUTTER_INTEGRATION_GUIDE.md`  
**Target Client**: Flutter Mobile Application (`D:\medsimbio\medisimbio-main`)  
**Date**: October 2026  

---

## 1. Network & Base URL Configuration

In Flutter mobile applications, connecting to `localhost` fails because the mobile runtime has its own loopback interface. Configure your network client (Dio, Http) using environment-specific host addresses:

```dart
class ApiConfig {
  static String get baseUrl {
    if (kReleaseMode) {
      return 'https://api.medisimbio.com/api/v1';
    }
    // Android Emulator host loopback alias
    if (Platform.isAndroid) {
      return 'http://10.0.2.2:8081/api/v1';
    }
    // iOS Simulator loopback
    if (Platform.isIOS) {
      return 'http://localhost:8081/api/v1';
    }
    // Default physical device IP or web
    return 'http://192.168.1.100:8081/api/v1';
  }
}
```

---

## 2. Authentication Headers for Mobile

### Development Mode:
Pass the patient's authenticated identity in the headers:
```dart
Map<String, String> get devHeaders => {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
  'X-User-Id': currentFirebaseUid ?? 'auth_pat_mobile_1',
  'X-User-Role': 'PATIENT',
  'Authorization': 'Bearer ${currentFirebaseUid ?? "auth_pat_mobile_1"}',
};
```

---

## 3. Flutter Screen-to-Backend API Mapping

| Flutter Screen / User Journey | HTTP Method & Endpoint | Payload / Parameters | Notes & Invariants |
|---|---|---|---|
| **Onboarding / Profile Setup** | `POST /api/v1/patients` | `fullName`, `email`, `phoneNumber`, `gender`, `dateOfBirth` | Creates patient profile record |
| **Profile Screen** | `GET /api/v1/patients/me` | None | Returns profile & existing Med-ID |
| **Med-ID Card Screen** | `POST /api/v1/patients/me/med-id` | None | Issues verifiable Luhn Mod-10 Med-ID (`MID-XXXX-XXXX-X`) |
| **Doctor Discovery** | `GET /api/v1/providers` | `?specialtyId=<id>&city=<city>` | Paginated provider summary list |
| **Slot Picker Screen** | `GET /api/v1/providers/{id}/slots` | `?date=YYYY-MM-DD&hospitalId=<id>` | Computes bookable 15/30-minute consultation slots |
| **Booking Confirmation** | `POST /api/v1/appointments` | `providerId`, `hospitalId`, `appointmentDate`, `startTime`, `endTime` | Returns booking reference |
| **Appointments History** | `GET /api/v1/appointments/me` | `?page=0&size=20&status=CONFIRMED` | Paginated appointments list |
| **Appointment Details Screen**| `GET /api/v1/appointments/{id}` | None | Full booking & doctor details |
| **Cancel Appointment** | `POST /api/v1/appointments/{id}/cancel` | `reason` | Releases reserved slot |
| **Reschedule Appointment** | `POST /api/v1/appointments/{id}/reschedule` | `appointmentDate`, `startTime`, `endTime` | Atomic reschedule |
| **Medical Timeline Screen** | `GET /api/v1/patients/me/medical-record/timeline` | `?page=0&size=20` | Returns 4-way unified timeline events |
| **Prescriptions Screen** | `GET /api/v1/patients/me/prescriptions` | `?page=0&size=20` | Finalized prescriptions only (drafts protected) |
| **Order Medicines Flow** | `POST /api/v1/prescriptions/{id}/dispensing-orders` | `deliveryAddress`, `pharmacyNotes` | Places pharmacy fulfillment order |
| **Dispensing Orders List** | `GET /api/v1/patients/me/dispensing-orders` | `?page=0&size=20` | Status: `PENDING`, `ACCEPTED`, `DISPENSED` |
| **AI Symptom Triage Flow** | `POST /api/v1/ai/triage` | `symptoms`, `description`, `severityScale` | Returns urgency and suggested specialty |

---

## 4. Mobile Code Example: Loading the Longitudinal Medical Timeline

```dart
Future<TimelinePage> fetchMedicalTimeline({int page = 0, int size = 20}) async {
  final response = await dio.get(
    '${ApiConfig.baseUrl}/patients/me/medical-record/timeline',
    queryParameters: {'page': page, 'size': size},
    options: Options(headers: devHeaders),
  );

  if (response.statusCode == 200 && response.data['success'] == true) {
    return TimelinePage.fromJson(response.data['data']);
  } else {
    throw Exception(response.data['message'] ?? 'Failed to load timeline');
  }
}
```

### Event Type Parsing:
The timeline event objects include an `eventType` field with one of four values:
1. `ENCOUNTER`: Clinical doctor visit. Render with doctor name, facility, and chief complaint.
2. `DIAGNOSIS`: Clinical finding. Render with ICD-10 code and diagnosis description.
3. `VITALS`: Recorded measurements. Render with blood pressure, pulse, SpO2, and temperature.
4. `PRESCRIPTION`: Finalized medication order. Render with medication list and pharmacy order button.
