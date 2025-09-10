import { Component } from '@angular/core';
import { FooterComponent } from '../../layout/footer/footer.component';
import { HeaderComponent } from '../../layout/header/header.component';
@Component({
  selector: 'app-inforshop',
  standalone: true,
  imports: [HeaderComponent, FooterComponent],
  templateUrl: './inforshop.component.html',
  styleUrl: './inforshop.component.scss'
})
export class InforshopComponent {

}
