import 'package:flutter/material.dart';

class PeriodSlider extends StatelessWidget {
  final double initialValue;
  final ValueChanged<double> onChanged;

  PeriodSlider({required this.initialValue, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    return Slider(
      min: 12,
      max: 48, // Changed from 60 to 48 months
      divisions: 36, // Changed from 48 to 36 (48 - 12 = 36 possible division steps)
      value: initialValue,
      onChanged: onChanged,
      label: '${initialValue.round()} months',
    );
  }
}