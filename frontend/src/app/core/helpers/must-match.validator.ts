
import { ValidatorFn, AbstractControl } from '@angular/forms';

export function MustMatch(controlName: string, matchingControlName: string): ValidatorFn {
  return (group: AbstractControl): { [key: string]: any } | null => {
    const control = group.get(controlName);
    const matchingControl = group.get(matchingControlName);

    if (!control || !matchingControl) {
      return null; // trả về null ở đây nếu không tìm thấy điều khiển; 
    }

    // trả về null nếu trình xác thực khác đã tìm thấy lỗi trên matchingControl
    if (matchingControl.errors && !matchingControl.errors['mustMatch']) {
      return null;
    }

    // đặt lỗi trên matchingControl nếu xác thực không thành công
    if (control?.value !== matchingControl?.value) {
      matchingControl.setErrors({ mustMatch: true });
      return { mustMatch: true };
    } else {
      matchingControl.setErrors(null);
      return null;
    }
  };
}